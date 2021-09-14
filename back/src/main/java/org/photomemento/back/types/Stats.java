package org.photomemento.back.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.photomemento.back.service.SystemService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class Stats {

    private final SystemData system;
    private final TimesData times;
    private final OsData os;
    private final MultiTaskData threads;
    private final Map<String,QueueData> queues;
    private final String javaVersion;

    @Builder
    @Getter
    public static class SystemData {
        private final long maxMemory;
        private final long totalMemory;
        private final long freeMemory;
        private final long usedMemory;
        private final String maxDisplayMemory;
        private final String totalDisplayMemory;
        private final String freeDisplayMemory;
        private final String usedDisplayMemory;
        private final float systemCpu;
        private final float processCpu;
    }

    @Builder
    @Getter
    public static class TimesData {
        private final String upTime;
        private final Instant startTime;
    }

    @Builder
    @Getter
    public static class OsData {
        private final String architecture;
        private final String name;
        private final String version;
        private final SystemService.OS_TYPE type;
    }

    @Builder
    @Getter
    public static class MultiTaskData {
        private final int totalActiveThreads;
        private final List<String> stackTraceSummary;
    }

    @Builder
    @Getter
    @Setter
    public static class QueueData {
        private int elsDetected;
        private int elsWaitingProcess;
        private int elsProcessedOk;
        private int elsProcessedError;
        private String elsProcessedLastId;
        private int threadsRunning;
        private int threadsMax;
    }
}
