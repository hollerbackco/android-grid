package com.moziy.hollerback.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.moziy.hollerback.util.AppEnvironment;

public class VolleySingleton {
    private static final String TAG = VolleySingleton.class.getSimpleName();
    private static VolleySingleton mInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private ImageCache mImageCache;

    private VolleySingleton(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageCache = new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(AppEnvironment.MEMORY_CACHE_SIZE);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);

            }

            public Bitmap getBitmap(String url) {

                return mCache.get(url);

            }
        };
        mImageLoader = new ImageLoader(this.mRequestQueue, mImageCache);
    }

    public static VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return this.mImageLoader;
    }

    public ImageCache getImageCache() {
        return mImageCache;
    }

    public static class HBImageLoader extends ImageLoader {

        public HBImageLoader(RequestQueue queue, ImageCache imageCache) {
            super(queue, imageCache);
        }

    }

}
