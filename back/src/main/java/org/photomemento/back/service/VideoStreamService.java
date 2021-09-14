package org.photomemento.back.service;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.exceptions.api.ServerErrorException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

import static org.photomemento.back.types.Constants.*;

@Service
@Slf4j
public class VideoStreamService {
    public static final int BYTE_RANGE = 1024;

    /**
     * Prepare the content
     */
    public ResponseEntity<byte[]> prepareContent(File videoFilePath, String format, String range, boolean isDownload, String fileName) {
        long rangeStart = 0;
        long rangeEnd;
        byte[] data;
        Long fileSize;
        try {
            fileSize = getFileSize(videoFilePath);
            //Not range? return full content
            if (range == null) {
                ResponseEntity.BodyBuilder bod = ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, format)
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize)); // Read the object and convert it as bytes;
                if (isDownload)
                    bod.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
                return bod
                        .body(readByteRange(videoFilePath, rangeStart, fileSize - 1));

            }
            String[] ranges = range.split(MINUS);
            rangeStart = Long.parseLong(ranges[0].substring(6));
            rangeEnd = ranges.length > 1 ?
                    Long.parseLong(ranges[1]) :
                    fileSize - 1;

            if (fileSize < rangeEnd)
                rangeEnd = fileSize - 1;

            data = readByteRange(videoFilePath, rangeStart, rangeEnd);
        } catch (IOException e) {
            String msg = String.format("Exception while reading the file: %s, due to: %s", videoFilePath, e.getMessage());
            log.error(msg, e);
            throw new ServerErrorException(msg, e);
        }
        //Range? return part of video
        String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, format)
                .header(HttpHeaders.ACCEPT_RANGES, BYTES)
                .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                .header(HttpHeaders.CONTENT_RANGE, BYTES + WHITESPACE + rangeStart + MINUS + rangeEnd + SLASH + fileSize)
                .body(data);
    }

    /**
     * ready file byte by byte.
     *
     * @param videoFilePath String.
     * @param start         long.
     * @param end           long.
     * @return byte array.
     * @throws IOException exception.
     */
    public byte[] readByteRange(File videoFilePath, long start, long end) throws IOException {
        try (InputStream inputStream = new FileInputStream(videoFilePath);
             ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[BYTE_RANGE];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1)
                bufferedOutputStream.write(data, 0, nRead);
            bufferedOutputStream.flush();
            byte[] result = new byte[(int) (end - start) + 1];
            System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0, result.length);
            return result;
        }
    }


    /**
     * Content length.
     *
     * @param videoFilePath String.
     * @return Long.
     */
    public Long getFileSize(File videoFilePath) {
        return Optional.ofNullable(videoFilePath)
                .map(file -> videoFilePath.length())
                .orElse(0L);
    }
}