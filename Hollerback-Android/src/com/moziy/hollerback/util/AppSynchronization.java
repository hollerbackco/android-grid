package com.moziy.hollerback.util;

import java.util.concurrent.Semaphore;

public class AppSynchronization {

    public static Semaphore sSyncSemaphore = new Semaphore(1); // only a single client can run

    // synchronize the video upload intent service and the passive upload service
    public static Semaphore sVideoUploadSemaphore = new Semaphore(1);

}
