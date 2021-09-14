package org.photomemento.back.monitoring.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.types.ThreadWorker;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

@Service
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
@Getter
public class ProcessorQueueService {
    private Map<String, PriorityBlockingQueue<?>> queues = new HashMap<>(); //NOSONAR
    private Map<String, ThreadWorker<?>> workers = new HashMap<>(); //NOSONAR

    public <T extends Comparable<T>> ThreadWorker<T> newQueueForClassWithWorkers(String id, int workersNo, int delay, ProcessorQueueServiceProcessor<T> processor, boolean isReverseOrder) {
        if (delay == 0) throw new PhotoMementoError(String.format("Error initializing workers for %s, expected a value greater than 0 for delay, \"%s\" was set", id, delay));
        if (workersNo == 0) throw new PhotoMementoError(String.format("Error initializing workers for %s, expected a value greater than 0 for workers, \"%s\" was set", id, workersNo));

        final PriorityBlockingQueue<T> queue = new PriorityBlockingQueue<T>(11, new CustomComparator<>(isReverseOrder));//NOSONAR
        queues.put(id, queue);
        ThreadWorker<T> threadWorker = new ThreadWorker(id, queue, workersNo, delay, processor);
        workers.put(id, threadWorker);
        threadWorker.start();
        return threadWorker;
    }

    public interface ProcessorQueueServiceProcessor<T> {
        boolean processQueueItem(T processable);
    }

    private static class CustomComparator<T extends Comparable<T>> implements Comparator<T> {
        private final boolean reverseOrder;

        public CustomComparator(boolean reverseOrder) {
            this.reverseOrder = reverseOrder;
        }

        @Override
        public int compare(T o1, T o2) {
            return o1 == null ?
                    1 :
                    o2 == null ? //NOSONAR
                            -1 :
                            reverseOrder ? //NOSONAR
                                    -1 * o1.compareTo(o2) :
                                    o1.compareTo(o2);
        }
    }
    //
    //    public static void main(String... args) {
    //        ThreadWorker<OnEventProcess> worker = (new ProcessorQueueService())
    //                .newQueueForClassWithWorkers("some", 1, 100, processable -> System.out.println("Detected " + JsonUtils.toJson(processable)));
    //        worker.offer(OnEventProcess.builder().file(new File("/some_url/b/23.jpg")).build());
    //        worker.offer(OnEventProcess.builder().file(new File("/some_url/a/453.jpg")).build());
    //        worker.offer(OnEventProcess.builder().file(new File("/some_url/c/a/23.jpg")).build());
    //        worker.offer(OnEventProcess.builder().file(new File("/some_url/c/23.jpg")).build());
    //        worker.start();
    //    }
}
