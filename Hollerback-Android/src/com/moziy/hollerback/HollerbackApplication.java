package com.moziy.hollerback;

import com.activeandroid.ActiveAndroid;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moziy.hollerback.util.DataModelManager;

public class HollerbackApplication extends com.activeandroid.app.Application {
    private static HollerbackApplication sInstance = null;

    private static DataModelManager sDataModelManager = null;
    private ObjectMapper mObjectMapper;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        ActiveAndroid.setLoggingEnabled(true);

        initObjectMapper();

        sDataModelManager = new DataModelManager();

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
    public void onTerminate() {
        // TODO Auto-generated method stub
        super.onTerminate();
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

}
