package com.moziy.hollerback.connection;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Application;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.util.AppEnvironment;

public class HollerbackAsyncClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    private static HollerbackAsyncClient sInstance;

    private HollerbackAsyncClient() {
    }

    public static HollerbackAsyncClient getInstance() {
        if (sInstance == null) {
            sInstance = new HollerbackAsyncClient();
            setHeaders();

            client.setTimeout(10000);
            client.setMaxRetriesAndTimeout(3, 10000);
            if (AppEnvironment.getInstance().ENV == AppEnvironment.ENV_DEVELOPMENT && HollerbackApplication.getInstance().getResources().getBoolean(R.bool.ENABLE_PROXY)) {
                Application app = HollerbackApplication.getInstance();
                String url = app.getResources().getString(R.string.PROXY_URL);
                int port = app.getResources().getInteger(R.integer.PROXY_PORT);
                client.setProxy(url, port);
            }
        }
        return sInstance;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        LogUtil.i("Get Request: " + getAbsoluteUrl(url));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, JSONObject object, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        // params is a JSONObject
        StringEntity se = null;
        try {
            se = new StringEntity(params.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(null, getAbsoluteUrl(url), se, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return AppEnvironment.getInstance().BASE_URL + HollerbackAPI.API_SUFFIX + relativeUrl;
    }

    private static void setHeaders() {
        client.addHeader(Header.PLATFORM, "android");
        client.addHeader(Header.ANDROID_OS_VERSION, Build.VERSION.RELEASE + ";;" + Build.VERSION.SDK_INT);
        client.addHeader(Header.ANDROID_APP_VERSION, AppEnvironment.APP_VERSION_CODE + ";;" + AppEnvironment.APP_VERSION_NAME);
        client.addHeader(Header.ANDROID_MODEL_NAME, Build.MANUFACTURER + ";;" + Build.MODEL);
    }

    private interface Header {
        public static final String PLATFORM = "PLATFORM";
        public static final String ANDROID_OS_VERSION = "ANDROID_OS_VERSION";
        public static final String ANDROID_APP_VERSION = "ANDROID_APP_VERSION";
        public static final String ANDROID_MODEL_NAME = "ANDROID_MODEL_NAME";

    }

}
