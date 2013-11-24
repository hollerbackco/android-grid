package com.moziy.hollerback.camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * An Async Camera Manager
 * @author sajjad
 *
 */

public class CameraManager extends Thread {
    private static CameraManager sInstance;

    private Looper mLooper;
    public Handler mHandler;

    public static CameraManager getInstance() {
        if (sInstance == null) {
            sInstance = new CameraManager();
            sInstance.start();
        }

        return sInstance;
    }

    private CameraManager() {
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new CameraHandler();
        Looper.loop();
        mLooper = Looper.myLooper();
    }

    public static class CameraHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }

        }
    }
}
