package com.moziy.hollerback.service.task;

import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.moziy.hollerback.util.HBFileUtil;

public class S3DownloadTask implements Task {

    private Task.Listener mTaskListener;
    private AmazonS3Client mS3Client;
    private GetObjectRequest mS3GetRequest;
    private boolean mIsSuccess = true;
    private boolean mIsFinished = false;

    public S3DownloadTask(AmazonS3Client S3Client, GetObjectRequest s3GetRequest) {
        mS3Client = S3Client;
        mS3GetRequest = s3GetRequest;
    }

    @Override
    public void run() {
        S3ObjectInputStream is = null;
        FileOutputStream fout = null;
        try {

            S3Object s3Object = mS3Client.getObject(mS3GetRequest);
            // lets get the output file information
            String key = s3Object.getKey();

            // lets create the file with the key
            fout = new FileOutputStream(HBFileUtil.getOutputVideoFile(key));

            byte[] buffer = new byte[1024];

            is = s3Object.getObjectContent();
            int readCount = -1;
            while ((readCount = is.read(buffer)) > -1) {
                fout.write(buffer, 0, readCount); // write to the file
            }

        } catch (AmazonClientException e) {
            mIsSuccess = false;

        } catch (IOException e) {
            mIsSuccess = false;

        } finally {

            try {
                if (is != null) {
                    is.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                mIsSuccess = false;
                e.printStackTrace();
            }

        }

        synchronized (this) {
            mIsFinished = true;
        }
    }

    @Override
    public boolean isSuccess() {

        return mIsSuccess;
    }

    public void setTaskListener(Task.Listener mListener) {
        mTaskListener = mListener;

    }

    @Override
    public Listener getTaskListener() {

        return mTaskListener;
    }

    @Override
    public boolean isFinished() {
        return mIsFinished;
    }

}
