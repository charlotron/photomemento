package org.photomemento.back.util;

import org.photomemento.back.exceptions.InvalidStateError;

import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    private ThreadUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static void sleepSecs(long secs){
        sleep(TimeUnit.SECONDS.toMillis(secs));
    }

    public static void sleep(long millis){
        try{
            Thread.sleep(millis);
        }
        catch (InterruptedException e){//NOSONAR
            //Don't mind just interrupt
        }
    }

    public static void runLater(long millis, Runnable runnable){
        if(millis>0)
            sleep(millis);
        runnable.run();
    }

    public static void runLaterAsync(long millis, Runnable runnable){
        (new Thread(()->runLater(millis,runnable))).start();
    }
}
