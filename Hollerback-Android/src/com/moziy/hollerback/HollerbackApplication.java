package com.moziy.hollerback;

import android.content.Intent;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Set;
import com.activeandroid.query.Update;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.BackgroundHelper;
import com.moziy.hollerback.lifecycle.AppLifecycle;
import com.moziy.hollerback.lifecycle.AppLifecycle.AppIdleListener;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.BgDownloadService;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.util.DataModelManager;

public class HollerbackApplication extends com.activeandroid.app.Application {
    private static HollerbackApplication sInstance = null;
    private static final String TAG = HollerbackApplication.class.getSimpleName();
    private static DataModelManager sDataModelManager = null;
    private ObjectMapper mObjectMapper;
    private AppLifecycle mLifecycle;

    @Override
    public void onCreate() {
        super.onCreate();

        ActiveAndroid.setLoggingEnabled(true);
        initObjectMapper();

        sDataModelManager = new DataModelManager();
        mLifecycle = new AppLifecycle();
        mLifecycle.registerIdleListener(mIdleListener);

        clearAllTransactingModel();

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

    public DataModelManager getDM() {
        return sDataModelManager;
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
        Set updateStatement = new Update(VideoModel.class).set(ActiveRecordFields.C_VID_TRANSACTING + "=?", 0);
        ActiveAndroidUpdateTask updateTask = new ActiveAndroidUpdateTask(updateStatement);
        TaskExecuter exeucter = new TaskExecuter();
        exeucter.executeTask(updateTask);

    }

    private AppLifecycle.AppIdleListener mIdleListener = new AppIdleListener() {

        @Override
        public void onIdle() {

            // launch the background download service
            Intent intent = new Intent();
            intent.setClass(HollerbackApplication.this, BgDownloadService.class);
            startService(intent);

        }
    };

}
