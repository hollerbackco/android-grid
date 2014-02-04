package com.moziy.hollerback.service.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import android.content.res.Resources;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AppEnvironment;

public class HttpDownloadTask extends AbsTask {
    protected String mSource;
    protected File mDestination;

    public HttpDownloadTask() {
    }

    public HttpDownloadTask(String src, File dest) {
        mSource = src;
        mDestination = dest;
    }

    @Override
    public void run() {
        BufferedInputStream bufIs = null;
        FileOutputStream fout = null;
        HttpURLConnection conn = null;
        try {
            Resources res = HollerbackApplication.getInstance().getResources();

            if (AppEnvironment.getInstance().ENV == AppEnvironment.ENV_DEVELOPMENT && res.getBoolean(R.bool.ENABLE_PROXY)) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(res.getString(R.string.PROXY_URL), res.getInteger(R.integer.PROXY_PORT)));
                conn = (HttpURLConnection) (new URL(mSource).openConnection(proxy));
            } else {
                conn = (HttpURLConnection) (new URL(mSource).openConnection());
            }

            conn.setDoInput(true); // read/download
            conn.connect(); // connect to the endpoint

            bufIs = new BufferedInputStream(conn.getInputStream()); // get the input stream

            // create the output stream associated with the file
            fout = new FileOutputStream(mDestination);
            int contentLength = conn.getContentLength();
            int totalRead = 0;

            byte buf[] = new byte[1024];
            int readCount = 0;
            while ((readCount = bufIs.read(buf)) > -1) {
                fout.write(buf, 0, readCount);

                // publish the progress
                if (contentLength > 0) {
                    totalRead += readCount;
                    onProgress((int) (((float) totalRead / (float) contentLength) * 100));
                }
            }

        } catch (MalformedURLException e) {
            mIsSuccess = false;
            e.printStackTrace();
        } catch (IOException e) {
            mIsSuccess = false;
            e.printStackTrace();
        } finally {
            try {
                if (bufIs != null) {
                    bufIs.close();
                }
                if (fout != null) {
                    fout.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }

            } catch (IOException e) {
                mIsSuccess = true;
                e.printStackTrace();
            }

            mIsFinished = true;
        }

    }

    public File getDestination() {
        return mDestination;
    }

    public String getSource() {
        return mSource;
    }

    public void onProgress(int progress) {

    }

}
