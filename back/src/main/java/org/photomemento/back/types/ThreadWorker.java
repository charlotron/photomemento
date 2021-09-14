package org.photomemento.back.types;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.types.initializer.I;

import java.util.Arrays;
import java.util.concurrent.*;

@Slf4j
@Getter
public class ThreadWorker<T> {

    private final String id;
    private final PriorityBlockingQueue<T> queue;
    private final int workersNo;
    private final ProcessorQueueService.ProcessorQueueServiceProcessor<T> processor;
    private final int delay;
    private final ScheduledThreadPoolExecutor executor;
    private int processedNo;
    private int errorNo;
    private T lastProcessed;

    public ThreadWorker(String id, PriorityBlockingQueue<T> queue, int workersNo, int delay, ProcessorQueueService.ProcessorQueueServiceProcessor<T> processor) {
        this.id = id;
        this.queue = queue;
        this.workersNo = workersNo;
        this.processor = processor;
        this.delay = delay;
        this.executor = new ScheduledThreadPoolExecutor(workersNo, new CustomThreadFactory());
    }

    public void start() {
        for (int i = 0; i < workersNo; i++) {
            final int workerNo = i;
            executor.scheduleWithFixedDelay(() -> {
                try {
                    lastProcessed = queue.take();
                    if (!processor.processQueueItem(lastProcessed))
                        errorNo++;
                    else
                        processedNo++;
                } catch (InterruptedException ie) { //NOSONAR
                    //NON IMPORTANT
                } catch (Exception | Error e) { //NOSONAR
                    errorNo++;
                    log.error(String.format("[thread#%s] Error was thrown inside Thread Worker: %s", workerNo, e.getMessage()), e);
                }
            }, 0, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void offer(T processable) {
        if (!this.queue.contains(processable)) //Unique
            this.queue.offer(processable); //NOSONAR
    }

    public class CustomThreadFactory implements ThreadFactory {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            return I.ofTapGet(
                    defaultFactory.newThread(r),
                    t -> t.setUncaughtExceptionHandler(new CustomUncaughtExceptionHandler()));
        }
    }

    class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable t) {
            errorNo++;
            if (t != null)
                log.error(String.format("Uncaught exception is detected! %s st: %s ", t, Arrays.toString(t.getStackTrace())), t);
        }
    }
}
