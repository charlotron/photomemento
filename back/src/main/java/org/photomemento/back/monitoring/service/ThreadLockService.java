package org.photomemento.back.monitoring.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;

/**
 * This class ensures a lock is properly set when trying to modify a "media" based on its id
 */
@Service
@Slf4j
public class ThreadLockService {
    private final MediaRepository mediaRepository;
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    public ThreadLockService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public <T extends Media> boolean lockRefreshFromDbRun(final String mediaId, final Class<T> expectedClass, final ThreadSafeProcessor<T> processor, long maxTimeSecs) {
        return threadSafeProcessById(mediaId, id -> {
            Optional<Media> opt;
            //---- RETRIEVE FROM DB
            try {
                //Ensure it is in sync with database
                opt = mediaRepository.findById(id);
                if (opt.isEmpty() || !expectedClass.isInstance(opt.get())) return false;
            } catch (Exception | Error e) { //NOSONAR
                log.error(String.format("[%s] Could not retrieve media from database, id: %s", processor.getClass().getSimpleName(), id), e);
                return false;
            }

            //---- CAST
            T mediaFile;
            try {
                //noinspection unchecked
                mediaFile = (T) opt.get();
            } catch (Exception | Error e) { //NOSONAR
                log.error(String.format("[%s] Cannot cast media with id: %s, to type: %s", processor.getClass().getSimpleName(), id, expectedClass.getSimpleName()), e);
                return false;
            }

            //---- PROCESS
            try {
                return processor.process(mediaFile);
            } catch (Exception | Error e) { //NOSONAR
                log.error(String.format("[%s] Cannot process media with id: %s", processor.getClass().getSimpleName(), id), e);
                return false;
            }
        }, maxTimeSecs);
    }

    /**
     * Returns a lock when available
     *
     * @param id          the id to lock by
     * @param processor   the code to be executed
     * @param maxTimeSecs how many seconds will be allowed to execute before killing the process (to prevent deadlocks due to dead connection or something like), use 0 to prevent interruption
     */
    @SneakyThrows
    public boolean threadSafeProcessById(String id, ThreadSafeProcessor<String> processor, long maxTimeSecs) {
        if (!StringUtils.hasText(id)) return false;

        Lock lock;
        boolean acquired = false;
        //Lock over map of locks
        synchronized (locks) {
            //Check if lock already exists
            lock = locks.get(id); //NOSONAR
            //Non existent? then create a lock
            if (lock == null) {
                lock = new ReentrantLock();
                lock.lock();
                locks.put(id, lock);
                acquired = true;
            }
        }
        //If it has been created just now only I have to capture all of the exceptions properly
        if (acquired) {
            try {
                if (maxTimeSecs > 0) {
                    AtomicBoolean res = new AtomicBoolean();
                    await()
                            .atMost(Duration.of(maxTimeSecs, ChronoUnit.SECONDS))
                            .until(() -> {
                                try {
                                    res.set(processor.process(id));
                                } catch (Exception | Error e) { //NOSONAR
                                    log.error(String.format("[%s] Error while processing: %s", processor.getClass().getSimpleName(), id), e);
                                    res.set(false);
                                }
                                return true;
                            });
                    return res.get();
                } else
                    return processor.process(id);
            } catch (InterruptedException ie) {
                throw ie;
            } catch (Exception | Error e) { //NOSONAR
                log.error(String.format("[%s] Error while processing: %s", processor.getClass().getSimpleName(), id), e);
                return false;
            } finally {
                //Remove lock
                synchronized (locks) {
                    locks.remove(id);
                    lock.unlock();
                }
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            lock.tryLock(30, TimeUnit.SECONDS); //NOSONAR
            return threadSafeProcessById(id, processor, maxTimeSecs);
        }
    }

    @FunctionalInterface
    public interface ThreadSafeProcessor<T> {
        boolean process(T mediaFile) throws InterruptedException;
    }


    public static void main(String... args) throws InterruptedException {
        ThreadLockService mediaLockService = new ThreadLockService(null);

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("1", id -> {
                    System.out.println("[1] Acquiring lock for id: " + id);
                    System.out.println("[1] I am throwing some exception so no \"releasing lock for id will be shown\": " + id);
                    throw new RuntimeException("some error");
                }, 0)
        )).start();

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("2", id -> {
                    System.out.println("[1.2] Acquiring lock for id: " + id);
                    System.out.println("[1.2] Waiting 2 secs then release: " + id);
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                    System.out.println("[1.2] Releasing lock for id: " + id);
                    return true;
                }, 0)
        )).start();

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("1", id -> {
                    System.out.println("[2] Acquiring lock for id: " + id);
                    System.out.println("[2] I am going to be interrupted so no \"releasing lock for id will be shown\": " + id);
                    throw new InterruptedException("some error");
                }, 0)
        )).start();

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("1", id -> {
                    System.out.println("[3] Acquiring lock for id: " + id);
                    System.out.println("[3] I am going to be interrupted due to max time reached so no \"releasing lock for id will be shown\": " + id);
                    Thread.sleep(TimeUnit.SECONDS.toMillis(50));
                    return true;
                }, 30)
        )).start();

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("1", id -> {
                    System.out.println("[4] Acquiring lock for id: " + id);
                    System.out.println("[4] Forcing next thread to wait two cicles: " + id);
                    Thread.sleep(TimeUnit.SECONDS.toMillis(50));
                    System.out.println("[4] Releasing lock for id: " + id);
                    return true;
                }, 0)
        )).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(2));

        (new Thread(() ->
                mediaLockService.threadSafeProcessById("1", id -> {
                    System.out.println("[5] Acquiring lock for id: " + id);
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                    System.out.println("[5] Releasing lock for id: " + id);
                    return true;
                }, 0)
        )).start();
    }
}
