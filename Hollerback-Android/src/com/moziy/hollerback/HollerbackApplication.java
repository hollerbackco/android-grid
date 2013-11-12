package com.moziy.hollerback;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.GCMRegistrar;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.DataModelManager;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnGCMReceivedListener;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class HollerbackApplication extends com.activeandroid.app.Application {
    private static HollerbackApplication sInstance = null;

    private static DataModelManager sDataModelManager = null;
    private ObjectMapper mObjectMapper;
    public String regId;

    Handler mGCMHandler;

    OnGCMReceivedListener listener;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        initObjectMapper();

        sDataModelManager = new DataModelManager();
        mGCMHandler = new Handler();
        initImageLoader(getApplicationContext());

    }

    Runnable GCMFetcherRunnable = new Runnable() {

        @Override
        public void run() {
            if (regId == null) {
                regId = GCMRegistrar.getRegistrationId(HollerbackApplication.this);
                mGCMHandler.postDelayed(GCMFetcherRunnable, 1000);
            } else {
                listener.onGCMReceived(regId);
                mGCMHandler.removeCallbacks(GCMFetcherRunnable);
            }
        }
    };

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public void getGCM(OnGCMReceivedListener listener) {
        this.listener = listener;
        if (!"sdk".equals(Build.PRODUCT)) {
            registerGCM();
            mGCMHandler.post(GCMFetcherRunnable);
        }
    }

    public void registerGCM() {
        if (!"sdk".equals(Build.PRODUCT)) {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                GCMRegistrar.register(this, AppEnvironment.getInstance().GOOGLE_PROJECT_NUMBER);
                LogUtil.i("GCM Registering");
            } else {
                LogUtil.i("GCM Already registered: " + regId);
            }
        }
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
