package com.moziy.hollerback.lifecycle;

import android.os.Handler;

public class AppLifecycle {

    private static final long APP_IDLE_TIME = 10000L; // app will be idle in 10 seconds
    private final Handler mHandler = new Handler();
    private boolean mIsIdle = true;

    public void setActive() {
        mHandler.removeCallbacks(mUpdateRunnable);
        mIsIdle = false;
    }

    public void setInactive() {
        mHandler.removeCallbacks(mUpdateRunnable);
        mHandler.postDelayed(mUpdateRunnable, APP_IDLE_TIME);
    }

    public boolean isIdle() {
        return mIsIdle;
    }

    private Runnable mUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            mIsIdle = true;
        }
    };

}
