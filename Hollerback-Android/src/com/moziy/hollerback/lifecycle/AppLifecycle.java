package com.moziy.hollerback.lifecycle;

import java.util.HashSet;
import java.util.Set;

import android.os.Handler;
import android.util.Log;

public class AppLifecycle {
    private static final String TAG = AppLifecycle.class.getSimpleName();
    private static final long APP_IDLE_TIME = 10000L; // app will be idle in 10 seconds

    public interface AppIdleListener {
        public void onIdle();
    }

    private final Handler mHandler = new Handler();
    private boolean mIsIdle = true;

    private Set<AppIdleListener> mListeners = new HashSet<AppIdleListener>();

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

    public void registerIdleListener(AppIdleListener listener) {
        mListeners.add(listener);
    }

    public void unregisterIdleListener(AppIdleListener listener) {
        mListeners.remove(listener);
    }

    private Runnable mUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "app is idle");
            mIsIdle = true;

            // notify all listeners
            for (AppIdleListener listener : mListeners) {
                listener.onIdle();
            }
        }
    };

}
