package org.photomemento.back.adapter;

import org.photomemento.back.monitoring.provider.FileWatcherProvider;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.service.SystemService;
import org.photomemento.back.types.Stats;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StatsAdapter {

    private final SystemService systemSvc;
    private final ProcessorQueueService processorQueueService;
    private final FileWatcherProvider fileWatcherProvider;

    public StatsAdapter(
            SystemService systemSvc,
            ProcessorQueueService processorQueueService,
            FileWatcherProvider fileWatcherProvider
    ) {
        this.systemSvc = systemSvc;
        this.processorQueueService = processorQueueService;
        this.fileWatcherProvider=fileWatcherProvider;
    }

    public Stats newStats(boolean withThreads) {
        Stats.StatsBuilder builder = Stats.builder()
                .system(
                        Stats.SystemData.builder()
                                .maxMemory(systemSvc.getMaxMemory())
                                .totalMemory(systemSvc.getTotalMemory())
                                .freeMemory(systemSvc.getFreeMemory())
                                .usedMemory(systemSvc.getUsedMemory())
                                .maxDisplayMemory(systemSvc.getMaxDisplayMemory())
                                .totalDisplayMemory(systemSvc.getTotalDisplayMemory())
                                .freeDisplayMemory(systemSvc.getFreeDisplayMemory())
                                .usedDisplayMemory(systemSvc.getUsedDisplayMemory())
                                .maxDisplayMemory(systemSvc.getMaxDisplayMemory())
                                .systemCpu(systemSvc.getSystemCpu())
                                .processCpu(systemSvc.getProcessCpu())
                                .build()
                )
                .times(
                        Stats.TimesData.builder()
                                .startTime(systemSvc.getStartTime())
                                .upTime(systemSvc.getUpTime())
                                .build()
                )
                .javaVersion(systemSvc.getJavaVersion())
                .os(Stats.OsData.builder()
                        .architecture(systemSvc.getOSArchitecture())
                        .name(systemSvc.getOSName())
                        .type(systemSvc.getOSType())
                        .version(systemSvc.getOSVersion())
                        .build())
                .queues(this.processorQueueService.getWorkers()
                        .entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(),
                                Stats.QueueData.builder()
                                        .elsDetected(entry.getValue().getQueue().size())
                                        .elsWaitingProcess(entry.getValue().getExecutor().getQueue().size())
                                        .elsProcessedOk(entry.getValue().getProcessedNo())
                                        .elsProcessedError(entry.getValue().getErrorNo())
                                        .elsProcessedLastId(
                                                fileWatcherProvider.getRelativePath(
                                                        Optional.ofNullable(entry.getValue().getLastProcessed())
                                                                .map(Object::toString)
                                                                .orElse(null)))
                                        .threadsRunning(entry.getValue().getExecutor().getActiveCount())
                                        .threadsMax(entry.getValue().getWorkersNo())
                                        .build()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        Stats.MultiTaskData.MultiTaskDataBuilder threadsBuilder = Stats.MultiTaskData.builder()
                .totalActiveThreads(systemSvc.getTotalActiveThreads());
        if (withThreads)
            threadsBuilder
                    .stackTraceSummary(getStackTraceSummary(systemSvc));
        builder.threads(threadsBuilder.build());
        return builder.build();
    }

    //TODO: Check this old method to be improved
    public List<String> getStackTraceSummary(SystemService systemSvc) {
        Map<Thread, StackTraceElement[]> threadsStacks = systemSvc.getAllActiveThreads();
        Map<String, Integer> threadsNum = new HashMap<>();
        Map<String, TreeMap<String, Integer>> threadsStacksFormatted = new HashMap<>();
        for (Map.Entry<Thread, StackTraceElement[]> stack_entry : threadsStacks.entrySet()) {
            String key = stack_entry.getKey().getClass().getName();
            if (!StringUtils.hasText(key))
                key = "UNKNOWN";
            StackTraceElement[] stacks = stack_entry.getValue();
            StringBuilder sb = new StringBuilder();
            if (stacks.length > 0) {
                boolean isLocalProjectClassFound = false;
                for (int i = stacks.length - 1; i >= 0; i--) {
                    StackTraceElement stack = stacks[i];
                    String className = stack.getClassName();
                    String methodName = stack.getMethodName();
                    int lineNumber=stack.getLineNumber();
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (className.equalsIgnoreCase("java.util.TimerThread") || Thread.class.isAssignableFrom(clazz))
                            continue;
                        if (!isLocalProjectClassFound && className.startsWith("org.photomemento")) {
                            sb.setLength(0);
                            sb.append("(..)");
                            isLocalProjectClassFound = true;
                        }
                        if (sb.length() > 0)
                            sb.append(" >> ");
                        sb.append(clazz.getSimpleName()).append(".").append(methodName).append(":").append(lineNumber);
                    } catch (ClassNotFoundException e) { //NOSONAR, just ignore
                    }
                }
            }
            String line = sb.toString();
            if (!StringUtils.hasText(line))
                continue;
            TreeMap<String, Integer> similarLines = threadsStacksFormatted.get(key);
            int count = similarLines == null || similarLines.get(line) == null ? 1 : similarLines.get(line) + 1;
            if (similarLines == null) {
                similarLines = new TreeMap<>();
                threadsStacksFormatted.put(key, similarLines);
            }
            similarLines.put(line.intern(), count);

            Integer num = threadsNum.get(key);
            threadsNum.put(key, num == null ? 1 : num + 1);
        }

        ArrayList<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> t : threadsNum.entrySet()) {
            TreeMap<String, Integer> similarLines = threadsStacksFormatted.get(t.getKey());

            StringBuilder sb = new StringBuilder();
            if (similarLines != null && !similarLines.isEmpty()) {
                for (Map.Entry<String, Integer> similar_line : similarLines.entrySet()) {
                    if (sb.length() > 0) {
                        lines.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    sb.append("[").append(similar_line.getValue()).append("] ").append(similar_line.getKey());
                }
            }
            lines.add(sb.toString());
        }

        return lines;
    }
}
