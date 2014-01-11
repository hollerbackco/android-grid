package com.moziy.hollerback;

import android.content.Intent;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Set;
import com.activeandroid.query.Update;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.BackgroundHelper;
import com.moziy.hollerback.lifecycle.AppLifecycle;
import com.moziy.hollerback.lifecycle.AppLifecycle.AppIdleListener;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.BgDownloadService;
import com.moziy.hollerback.service.PassiveUploadService;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.service.task.TaskGroup;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.recovery.ResourceRecoveryUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class HollerbackApplication extends com.activeandroid.app.Application {
    private static HollerbackApplication sInstance = null;
    private static final String TAG = HollerbackApplication.class.getSimpleName();
    private ObjectMapper mObjectMapper;
    private AppLifecycle mLifecycle;

    @Override
    public void onCreate() {
        super.onCreate();
        if (AppEnvironment.getInstance().ENV == AppEnvironment.ENV_PRODUCTION) {
            Crashlytics.start(this);
            long userId = PreferenceManagerUtil.getPreferenceValue(HBPreferences.ID, -1L);
            if (userId != -1) {
                Crashlytics.setUserIdentifier(String.valueOf(userId));
            }
        }

        ActiveAndroid.setLoggingEnabled(true);
        initObjectMapper();

        mLifecycle = new AppLifecycle();
        mLifecycle.registerIdleListener(mIdleListener);

        clearAllTransactingModel();

        ResourceRecoveryUtil.init();

        BackgroundHelper.getInstance(); // create the looper for the camera manager

    }

    private void initObjectMapper() {
        mObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, false) //
                .configure(MapperFeature.AUTO_DETECT_GETTERS, false); //
        // perform any sort of configuration here

        // mObjectMapper.configure(DeserializationFeature., state)
    }

    public ObjectMapper getObjectMapper() {
        return mObjectMapper;
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        super.onTrimMemory(level);
    }

    public HollerbackApplication() {
        sInstance = this;
    }

    public static HollerbackApplication getInstance() {
        return sInstance;
    }

    public String s(int id) {
        return getResources().getString(id);
    }

    public AppLifecycle getAppLifecycle() {
        return mLifecycle;
    }

    /**
     * This method will clear all transacting model in the event the model
     * was set to transacting and the app got killed
     */
    public void clearAllTransactingModel() {
        // TODO - sajjad: needs to be tested
        Log.d(TAG, "clearing all transacting model.");
        TaskGroup group = new TaskGroup();

        Set updateStatement = new Update(VideoModel.class).set(ActiveRecordFields.C_VID_TRANSACTING + "=?", 0);
        ActiveAndroidUpdateTask updateTask = new ActiveAndroidUpdateTask(updateStatement);
        group.addTask(updateTask);

        // if we got killed while downloading, then mark all downloading fields as pending download
        updateStatement = new Update(VideoModel.class).set(ActiveRecordFields.C_VID_STATE + "=?", VideoModel.ResourceState.PENDING_DOWNLOAD).where(ActiveRecordFields.C_VID_STATE + "=?",
                VideoModel.ResourceState.DOWNLOADING);
        updateTask = new ActiveAndroidUpdateTask(updateStatement);
        group.addTask(updateTask);

        TaskExecuter exeucter = new TaskExecuter();
        exeucter.executeTask(group);

    }

    private AppLifecycle.AppIdleListener mIdleListener = new AppIdleListener() {

        @Override
        public void onIdle() {

            // launch the background download service
            Intent intent = new Intent();
            intent.setClass(HollerbackApplication.this, BgDownloadService.class);
            startService(intent);

            // launch the passive upload service
            intent = new Intent();
            intent.setClass(HollerbackApplication.this, PassiveUploadService.class);
            startService(intent);

        }
    };

}
