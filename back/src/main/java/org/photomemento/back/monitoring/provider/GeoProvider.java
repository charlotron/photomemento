package org.photomemento.back.monitoring.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.types.geo.OSMReverseGeocodeRes;
import org.photomemento.back.types.geo.OSMSearchRes;
import org.photomemento.back.types.initializer.I;
import org.photomemento.back.util.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
//https://nominatim.org/release-docs/develop/api/Overview/
public class GeoProvider {
    public static final String REVERSE_GEOCODING_URL = "https://nominatim.openstreetmap.org/reverse?format=geocodejson&lon=%s&lat=%s&email=%s";
    public static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search?format=json&%s=%s&email=%s";
    private final DecimalFormat decimalFormatter =
            I.ofTapGet(new DecimalFormat(), df -> {
                df.setMaximumFractionDigits(14);
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
            });

    @Value("${geo.openstreetmap.contactemail}")
    public String contactMail;
    @Value("${geo.openstreetmap.max.request.connection.timeout.millis:1000}")
    public int maxRequestConnectionTimeoutMillis;
    @Value("${geo.openstreetmap.max.request.read.timeout.millis:3000}")
    public int maxRequestReadTimeoutMillis;

    private RestTemplate restTemplate;
    private boolean init;

    @PostConstruct
    public GeoProvider init() {
        if (init) return this;

        restTemplate = I.of(new HttpComponentsClientHttpRequestFactory())
                .tap(factory -> {
                    if (maxRequestConnectionTimeoutMillis > 0) {
                        log.debug(String.format("set connection timeout to: %s ms", maxRequestConnectionTimeoutMillis));
                        factory.setConnectTimeout(maxRequestConnectionTimeoutMillis);
                        factory.setConnectionRequestTimeout(maxRequestConnectionTimeoutMillis);
                    }
                    if (maxRequestReadTimeoutMillis > 0) {
                        log.debug(String.format("set read timeout to: %s ms", maxRequestReadTimeoutMillis));
                        factory.setReadTimeout(maxRequestReadTimeoutMillis);
                    }
                })
                .mapGet(RestTemplate::new);
        init = true;
        return this;
    }

    public OSMReverseGeocodeRes reverseGeocoding(Double lon, Double lat) {
        if (!init) throw new PhotoMementoError("OIS info not init yet, retrying later.");
        final URI reverseGeoUri = getReverseGeoUri(lon, lat);
        log.debug(String.format("Reverse geocoding: %s", reverseGeoUri));
        String json = restTemplate.getForObject(reverseGeoUri, String.class);
        log.debug(String.format("Reverse geocoding response: %s", json));

        if (json==null || json.toLowerCase().contains("error")) {
            log.warn(String.format("Seems some error by nominatim while reverse geocoding, returning null response. Error: %s", json));
            return null;
        }

        return JsonUtils.toObj(json, new TypeReference<>() {
        });
    }

    private URI getReverseGeoUri(Double lon, Double lat) {
        return URI.create(String.format(
                REVERSE_GEOCODING_URL,
                decimalFormatter.format(lon),
                decimalFormatter.format(lat),
                URLEncoder.encode(contactMail, StandardCharsets.UTF_8)));
    }

    public OSMSearchRes searchOne(String filterByName, String filterByValue) {
        if (!init) throw new PhotoMementoError("OIS info not init yet, retrying later.");
        final URI searchUri = getSearchUri(filterByName, filterByValue);
        log.debug(String.format("Geo Search: %s", searchUri));
        String json = restTemplate.getForObject(searchUri, String.class);
        log.debug(String.format("Geo Search response: %s", json));

        if(json==null || json.contains("error"))
            return null;

        return I.of(JsonUtils.toObj(json, new TypeReference<List<OSMSearchRes>>() {}))
                .filter(list -> !list.isEmpty())
                .mapGet(list -> list.get(0));
    }


    private URI getSearchUri(String filterByName, String filterByValue) {
        return URI.create(String.format(SEARCH_URL,
                filterByName,
                URLEncoder.encode(filterByValue, StandardCharsets.UTF_8),
                URLEncoder.encode(contactMail, StandardCharsets.UTF_8)));
    }

    @SneakyThrows
    public static void main(String... args) {
        GeoProvider geoPrvdr = new GeoProvider();
        geoPrvdr.contactMail = "photomomento@photomemento.inv";
        Object res = geoPrvdr.init().reverseGeocoding(-3.8298052386145836, 40.35466775549514);
        log.info("Data: " + (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(res));
    }
}
