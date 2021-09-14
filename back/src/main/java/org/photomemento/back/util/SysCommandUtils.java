package org.photomemento.back.util;

import org.apache.commons.io.IOUtils;
import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SysCommandUtils {
    private static final String ERROR_STREAM = "ERROR";
    private static final String STANDARD_STREAM = "STANDARD";

    private SysCommandUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static String runCommand(String... commandParts) {
        if (commandParts == null || commandParts.length == 0) throw new PhotoMementoError("Error executing command: Unexpected empty commands list");
        List<String> cmds = Arrays.stream(commandParts).filter(StringUtils::hasText).collect(Collectors.toList());
        try {
            Process process = new ProcessBuilder()
                    .command(cmds)
                    .start();

            //Check normal stream
            String res = readStream(STANDARD_STREAM, process.getInputStream());
            if (StringUtils.hasText(res))
                return res;

            //Check error stream
            res = readStream(ERROR_STREAM, process.getErrorStream());
            if (StringUtils.hasText(res))
                throw new PhotoMementoError(String.format("There was an error while executing command, error returned by process is: %s", res));

            return null;
        } catch (Exception | Error e) { //NOSONAR
            throw new PhotoMementoError(String.format("Error executing command: '%s'", cmds), e);
        }
    }

    private static String readStream(String streamName, InputStream stream) {
        try (StringWriter writer = new StringWriter()) {
            IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
            return writer.toString();
        } catch (IOException e) {
            throw new PhotoMementoError(String.format("There was an error reading stream: %s, error: %s", streamName, e.getMessage()), e);
        }
    }
}
