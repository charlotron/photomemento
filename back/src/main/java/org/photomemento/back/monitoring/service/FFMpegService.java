package org.photomemento.back.monitoring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.util.JsonUtils;
import org.photomemento.back.util.SysCommandUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FFMpegService {

    public static final String PRINT_FORMAT = "-print_format";
    public static final String PRINT_FORMAT_JSON = "json";
    public static final String SHOW_FORMAT = "-show_format";
    public static final String SHOW_STREAMS = "-show_streams";
    public static final String INPUT_FILE = "-i";
    public static final String SECONDS_FOR_SHOT = "-ss";
    private static final String SECONDS_FOR_SHOT_FORMAT = "%02d:%02d:%02d";
    public static final String HOW_MANY_FRAMES_TAKE = "-vframes";
    public static final String TAKE_ONE_FRAME = "1";
    public static final String FORMAT_CFG = "-vf";
    public static final String ALWAYS_OVERWRITE_DESTINY = "-y";
    public static final String FORMAT_CFG_VALUE = "scale=%s:%s";
    public static final String HIDE_BANNER = "-hide_banner";
    public static final String LOG_LEVEL = "-loglevel";
    public static final String LOG_LEVEL_ERROR = "error";
    public static final String SECONDS_DURATION = "-t";
    public static final String NO_AUDIO = "-an";

    @Value("${ffmpeg.base.path}")
    private String ffmpegBasePath;
    @Value("${ffmpeg.binary.ffmpeg}")
    private String ffmpegBinaryFfmpeg;
    @Value("${ffmpeg.binary.ffprobe}")
    private String ffmpegBinaryFfprobe;

    @PostConstruct
    public void init() {
        if (!Files.exists(Path.of(getFFMPEG()))) throw new PhotoMementoError(String.format("Could not init FFMpegService, due to invalid ffmpeg bin location, not installed or not in given location: %s", getFFMPEG()));
        if (!Files.exists(Path.of(getFFPROBE()))) throw new PhotoMementoError(String.format("Could not init FFMpegService, due to invalid ffprobe bin location, not installed or not in given location: %s", getFFPROBE()));
    }

    public Map<String, Object> getMetadata(String sourceVideoPath) {
        String res = SysCommandUtils.runCommand(
                getFFPROBE(),
                PRINT_FORMAT,
                PRINT_FORMAT_JSON,
                SHOW_FORMAT,
                SHOW_STREAMS,
                sourceVideoPath);
        //System.out.println("Res: " + res);
        return JsonUtils.toObj(res, new TypeReference<>() {
        });
    }

    public String generateImageFromVideoAtPosition(String sourceVideoPath, String destPhotoPath, long startPositionSecs, int width, int height) {
        return SysCommandUtils.runCommand(
                getFFMPEG(),
                INPUT_FILE,
                sourceVideoPath,
                SECONDS_FOR_SHOT,
                getSecondsForShotValue(startPositionSecs > 0 ? startPositionSecs : 1),
                HOW_MANY_FRAMES_TAKE,
                TAKE_ONE_FRAME,
                FORMAT_CFG,
                String.format(FORMAT_CFG_VALUE, width, height),
                ALWAYS_OVERWRITE_DESTINY,
                destPhotoPath,
                HIDE_BANNER,
                LOG_LEVEL,
                LOG_LEVEL_ERROR);
    }

    public String resampleAndTransformVideo(String sourceVideoPath, String destVideoPath, Long startPositionSecs, Long lengthFromStart, int width, int height, boolean withAudio) {
        List<String> commandParts = new ArrayList<>(List.of(
                getFFMPEG(),
                INPUT_FILE,
                sourceVideoPath,
                FORMAT_CFG,
                String.format(FORMAT_CFG_VALUE, width, height)
        ));

        if (startPositionSecs != null && lengthFromStart != null) {
            commandParts.addAll(List.of(
                    SECONDS_FOR_SHOT,
                    getSecondsForShotValue(startPositionSecs > 0 ? startPositionSecs : 1),
                    SECONDS_DURATION,
                    getSecondsForShotValue(lengthFromStart > 0 ? lengthFromStart : 3)
            ));
        }

        if (!withAudio)
            commandParts.add(NO_AUDIO);

        //Transform configurations have to be done before the following (order matters!)

        commandParts.addAll(List.of(
                ALWAYS_OVERWRITE_DESTINY,
                destVideoPath,
                HIDE_BANNER,
                LOG_LEVEL,
                LOG_LEVEL_ERROR
        ));

        return SysCommandUtils.runCommand(commandParts.toArray(new String[0]));
    }

    private static String getSecondsForShotValue(long seconds) {
        if (seconds < 0) seconds = 0;
        long h = TimeUnit.SECONDS.toHours(seconds);
        long m = TimeUnit.SECONDS.toMinutes(seconds - TimeUnit.HOURS.toSeconds(h));
        long s = seconds - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m);
        return String.format(SECONDS_FOR_SHOT_FORMAT, h, m, s);
    }


    @SneakyThrows
    public static void main(String... args) {
        /*System.out.println("Seconds: " + getSecondsForShotValue(5));
        System.out.println("Seconds: " + getSecondsForShotValue(50));
        System.out.println("Seconds: " + getSecondsForShotValue(1800));
        System.out.println("Seconds: " + getSecondsForShotValue(5000));*/
        //
        //        FFMpegService svc = new FFMpegService();
        //        svc.ffmpegBasePath = "/usr/local/opt/ffmpeg/bin/";
        //        svc.ffmpegBinaryFfmpeg = "ffmpeg";
        //        svc.ffmpegBinaryFfprobe = "ffprobe";
        //
        //        final String sourceVideoPath = "back/data/we/VID_20210825_111551.mp4";
        //        final String thumbPath = "back/cache/resources/images/pic_VID_20210825_111551.jpg";
        //        final String destAnimThumbPath = "back/cache/resources/images/thmb_VID_20210825_111551.mp4";
        //
        //        FileUtils.makeParentDirs(new File(thumbPath));
        //
        //        //Read metadata as json
        //        final Map<String,Object> metadata = svc.getMetadata(sourceVideoPath);
        //        System.out.println("Meta: " + JsonUtils.toJson(metadata, true));
        //        Float duration = metadata.get("format").get("duration");
        //
        //        //Generate image thumb from video
        //        int startPositionSecs = (int) Math.floor(duration / 2);
        //        System.out.println("Generated shot" + svc.generateThumb(sourceVideoPath, thumbPath, startPositionSecs, 320, 320));
        //
        //        //Generate animated thumb from video
        //        int expectedDuration = 5;
        //        int start = Math.max((int) Math.floor(duration / 2) - expectedDuration, 0);
        //        int animThumbDuration = (int) Math.min(start + expectedDuration, duration);
        //        System.out.println("Generated animated thumb" + svc.resampleVideo(sourceVideoPath, destAnimThumbPath, start, animThumbDuration, 320, 320, true));
    }

    protected String getFFMPEG() {
        return ffmpegBasePath + ffmpegBinaryFfmpeg;
    }

    protected String getFFPROBE() {
        return ffmpegBasePath + ffmpegBinaryFfprobe;
    }
}
