package org.photomemento.back.monitoring.service.typeprocessors.media;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.adapter.GeoDataAdapter;
import org.photomemento.back.domain.entity.Location;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.GeoData;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.photomemento.back.monitoring.provider.GeoProvider;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.repository.location.LocationRepository;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.Constants;
import org.photomemento.back.types.ThreadWorker;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.photomemento.back.types.geo.OSMFeature;
import org.photomemento.back.types.geo.OSMProperties;
import org.photomemento.back.types.geo.OSMReverseGeocodeRes;
import org.photomemento.back.types.geo.OSMSearchRes;
import org.photomemento.back.types.initializer.I;
import org.photomemento.back.util.GeoUtils;
import org.photomemento.back.util.IdUtils;
import org.photomemento.back.util.NumberUtils;
import org.photomemento.back.util.ThreadUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class does the following:
 * - Parses metadata to resolve lat/lon
 * - Takes coords then calls the OSM to geo resolve the zone of the photo/video
 */
@Service
@Slf4j
public class GeoDataProcessor implements ProcessorQueueService.ProcessorQueueServiceProcessor<String> {

    public static final int MAX_POW_ERROR_COUNT_START = 10;
    public static final int MAX_POW_ERROR_COUNT_SUCCESSIVE = 5;
    public static final int SECONDS_OFFSET_RETRY = 10;

    @Value("${queue.geo.workers.num}")
    private int queueGeoWorkersNum;
    @Value("${queue.geo.workers.delay}")
    private int queueGeoWorkersDelay;
    @Value("${queue.geo.workers.max.processing.time.secs}")
    private long queueGeoWorkersMaxProcessingTimeSecs;

    private final MediaRepository mediaRepository;
    private final LocationRepository locationRepository;
    private final GeoProvider geoProvider;
    private final ThreadLockService mediaLockService;
    private final ProcessorQueueService processorQueueService;
    private final GeoDataAdapter geoDataAdapter;

    private ThreadWorker<String> threadWorker;
    private int errorCount = 0;

    public GeoDataProcessor(
            ProcessorQueueService processorQueueService,
            MediaRepository mediaRepository,
            LocationRepository locationRepository,
            GeoProvider geoProvider,
            ThreadLockService mediaLockService,
            GeoDataAdapter geoDataAdapter) {
        this.mediaRepository = mediaRepository;
        this.locationRepository = locationRepository;
        this.geoProvider = geoProvider;
        this.mediaLockService = mediaLockService;
        this.processorQueueService = processorQueueService;
        this.geoDataAdapter = geoDataAdapter;
    }

    @PostConstruct
    public void init() {
        threadWorker = processorQueueService.newQueueForClassWithWorkers(this.getClass().getSimpleName(), queueGeoWorkersNum, queueGeoWorkersDelay, this, true);
    }

    public void parseMetadataGeoInfo(Media media) {
        String latDegrees = media.getValueOfMetadata(Constants.GPS_LATITUDE, Constants.LATITUDE);
        if (!StringUtils.hasText(latDegrees))
            log.trace(String.format(Constants.COULD_NOT_FIND_PROPERTY_S_ON_METADATA_FOR_FILE_S, List.of(Constants.GPS_LATITUDE, Constants.LATITUDE), media.getPath()));

        String lonDegrees = media.getValueOfMetadata(Constants.GPS_LONGITUDE, Constants.LONGITUDE);
        if (!StringUtils.hasText(lonDegrees))
            log.trace(String.format(Constants.COULD_NOT_FIND_PROPERTY_S_ON_METADATA_FOR_FILE_S, List.of(Constants.GPS_LONGITUDE, Constants.LONGITUDE), media.getPath()));

        if (StringUtils.hasText(latDegrees) && StringUtils.hasText(lonDegrees)) {
            double[] coords = resolveCoords(latDegrees, lonDegrees);
            media.setLatitude(coords[0]);
            media.setLongitude(coords[1]);
        }
        //Else? do nothing
    }

    /**
     * Detects if coords are given as decimal or degrees, if is last one then convert to decimal
     */
    private double[] resolveCoords(String latDegrees, String lonDegrees) {
        return latDegrees.contains(Constants.DEGREE_CHAR) && lonDegrees.contains(Constants.DEGREE_CHAR) ?
                GeoUtils.degreesToDecimal(GeoUtils.getDegMinSec(latDegrees, lonDegrees)) :
                new double[]{
                        NumberUtils.parseAsDoubleFixingCommas(latDegrees),
                        NumberUtils.parseAsDoubleFixingCommas(lonDegrees)};
    }

    public void processLater(Media media) {
        if (!hasToBeParsed(media)) return;
        threadWorker.offer(media.getPath());
    }

    public boolean hasToBeParsed(Media media) {
        return media.getLongitude() != null || media.getLatitude() != null;
    }

    @Override
    public boolean processQueueItem(String mediaPath) {
        final String mediaId = IdUtils.getIdFromAbsPath(mediaPath);
        boolean res = mediaLockService.lockRefreshFromDbRun(
                mediaId,
                Media.class,
                this::geocodeAndUpdateMedia, queueGeoWorkersMaxProcessingTimeSecs);

        //Retry? NominatimApi could be down
        if (!res) {
            //TODO: PARAMETRIZE THIS, MAX TRYOUTS?
            long waitTimeErrorSecs = calculateWaitTimeOnErrors(MAX_POW_ERROR_COUNT_START) + SECONDS_OFFSET_RETRY;
            log.warn(String.format("Seems error calling nominatim api, retrying in %s seconds, for: %s", waitTimeErrorSecs, mediaPath));
            ThreadUtils.runLaterAsync(
                    TimeUnit.SECONDS.toMillis(waitTimeErrorSecs),
                    () -> threadWorker.offer(mediaPath));
        }
        return res;
    }

    private boolean geocodeAndUpdateMedia(Media media) {
        boolean isError = false;
        try {
            //Obtain address from latitude and longitude
            final GeoData geoData = reverseGeoCode(media);
            if (geoData != null && geoData.getZoomLevels() != null) {
                //Generate location (if needed)
                searchLocation(geoData);

                //All processing finished? decrease error count
                decreaseErrorCount();

                //SET STATUS
                media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.OK);
            } else
                //SET STATUS
                media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.N_A);
        } catch (Exception | Error e) { //NOSONAR
            increaseErrorCount();
            log.error(String.format("There was an error while processing geo data for: %s, error: %s", media.getPath(), e.getMessage()), e);
            isError = true;
            media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.FAIL);
        }

        // SAVE!
        try {
            mediaRepository.save(media);
        } catch (Exception | Error e) { //NOSONAR
            log.error(String.format("There was an error while saving geo data processing result for: %s, error: %s", media.getPath(), e.getMessage()), e);
            isError = true;
        }

        // Invalid data so stop here
        return !isError;
    }

    private void sleepIfNeeded(boolean waitWorkerSleep) {
        if (waitWorkerSleep)
            ThreadUtils.sleep(this.queueGeoWorkersDelay);
        if (errorCount > 0) {
            final long seconds = calculateWaitTimeOnErrors(
                    waitWorkerSleep ?
                            MAX_POW_ERROR_COUNT_SUCCESSIVE :
                            MAX_POW_ERROR_COUNT_START); //Max 2^10
            log.debug(String.format("[GEODATA REQUEST] Sleeping %s seconds, due to previous errors: %s, successive: %s", seconds, errorCount, waitWorkerSleep));
            ThreadUtils.sleep(TimeUnit.SECONDS.toMillis(seconds));
        }
    }

    private long calculateWaitTimeOnErrors(int maxPow) {
        return (long) Math.pow(2, Math.min(maxPow, errorCount));
    }

    private void increaseErrorCount() {
        errorCount++;
    }

    private void decreaseErrorCount() {
        if (errorCount > 0) errorCount--;
    }

    private GeoData reverseGeoCode(Media media) {
        sleepIfNeeded(false);

        return I.of(geoProvider.reverseGeocoding(media.getLongitude(), media.getLatitude()))
                .map(OSMReverseGeocodeRes::getFeatures)
                .filter(f -> !f.isEmpty())
                .map(f -> f.get(0))
                .map(OSMFeature::getProperties)
                .map(OSMProperties::getGeocoding)
                .map(geoDataAdapter::fromOsmGeocoding)
                .tapGet(media::setGeoData);
    }

    private void searchLocation(GeoData geoData) {
        for (ZoomLevels.ZOOM_LEVEL zoomLevel : ZoomLevels.ZOOM_LEVEL.values()) {
            String zoomLevelValue = geoData.getZoomLevels().getZoomLevel(zoomLevel);
            if (!StringUtils.hasText(zoomLevelValue) || //Not valid text to search by
                    locationRepository.findByZoomLevelAndName(zoomLevel, zoomLevelValue).isPresent()) //Already in database, just skip
                continue;

            //Cannot perform more than 1 request per sec to OIS -- too slow :(
            sleepIfNeeded(true);

            Optional.ofNullable(geoProvider.searchOne(zoomLevel.getOsmField(), zoomLevelValue))
                    .ifPresent(searchRes -> locationRepository.save(
                            Location.builder()
                                    .id(searchRes.getPlaceId())
                                    .latitude(searchRes.getLat())
                                    .longitude(searchRes.getLon())
                                    .name(zoomLevelValue)
                                    .zoomLevel(zoomLevel)
                                    .build()
                    ));

        }
    }

    /*
    private void saveLocation(Media media) {
        try {
            Location location = locationAdapter.fromMedia(geoGroupsField, media);
            if(location==null)
                throw new PhotoMementoError(String.format("Unable to generate location from media: %s", media.getPath()));
            locationRepository.save(location);
        } catch (Exception | Error e) { //NOSONAR
            log.error(String.format("There was an error while processing location for: %s, error: %s", media.getPath(), e.getMessage()), e);
        }
    }*/
}
