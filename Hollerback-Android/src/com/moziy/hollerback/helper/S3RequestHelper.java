package com.moziy.hollerback.helper;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.connection.RequestCallbacks.OnProgressListener;
import com.moziy.hollerback.connection.RequestCallbacks.OnS3UploadListener;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.view.CustomVideoView;

// TODO: Abstract the upload methods, verification and buckets

public class S3RequestHelper {
    private static final String TAG = S3RequestHelper.class.getSimpleName();
    public static AmazonS3Client s3Client;

    static {
        s3Client = new AmazonS3Client(new BasicAWSCredentials(AppEnvironment.getInstance().ACCESS_KEY_ID, AppEnvironment.getInstance().SECRET_KEY));
    }

    private static OnProgressListener mOnProgressListener;

    public String uploadFile(S3UploadParams params, String filePath) {
        return null;
    }

    public void registerOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    public void clearOnProgressListener() {
        mOnProgressListener = null;
    }

    /**
     * This method will upload a file to s3 and return the result object
     * Note that this method will not run on a background thread
     * @param fileName the local file name to upload
     * @param filePath the full path of the file to upload
     * @return the result or null if the upload failed
     */
    public static synchronized PutObjectResult uploadFileToS3(String fileName, String filePath) {

        PutObjectResult putObjectResult = null;
        // Put the image data into S3.
        try {
            // s3Client.createBucket(AppEnvironment.getPictureBucket());

            // Content type is determined by file extension.
            PutObjectRequest fileUploadRequest = new PutObjectRequest(AppEnvironment.getInstance().UPLOAD_BUCKET, fileName, new java.io.File(filePath));

            // TODO - Sajjad: If necessary, then provide progress with fileUploadRequest.setProgre..

            putObjectResult = s3Client.putObject(fileUploadRequest);

        } catch (AmazonServiceException ase) {

            putObjectResult = null; // set to null if anything goes wrong

            Log.d(TAG, "Amazon Error Message: " + ase.getMessage());

        } catch (AmazonClientException ace) {

            putObjectResult = null; // set to null if anything goes wrong

            Log.d(TAG, "Amazon Error Message: " + ace.getMessage());

        } catch (Exception exception) {
            exception.printStackTrace();
            putObjectResult = null; // set to null if anything goes wrong
            Log.d(TAG, "Amazon Error Message - General Exception");
        }

        return putObjectResult;
    }

    public void getS3URLParams(ArrayList<S3UploadParams> videos) {
        S3UploadParams[] videosArray = videos.toArray(new S3UploadParams[videos.size()]);
        new S3RequestHelper.S3GenerateUrlTask().execute(videosArray);
    }

    // TODO - Sajjad: What was the original intention of this AsyncTask?
    // Answer: presigned urls would allow individuals who have the url to access
    // the resource, but people who don't, won't be able to.
    public class S3GenerateUrlTask extends AsyncTask<S3UploadParams, Void, S3TaskResult> {

        protected S3TaskResult doInBackground(S3UploadParams... videos) {

            S3TaskResult result = new S3TaskResult();
            for (S3UploadParams uploadParams : videos) {
                try {
                    LogUtil.i("S3GenTask for " + uploadParams.getFileName());
                    // Ensure that the image will be treated as such.
                    ResponseHeaderOverrides override = new ResponseHeaderOverrides();

                    override.setContentType("video/mp4");
                    Date expirationDate = new Date(System.currentTimeMillis() + 3600000);

                    GeneratePresignedUrlRequest urlVideoRequest = new GeneratePresignedUrlRequest(AppEnvironment.getInstance().PICTURE_BUCKET, uploadParams.getFileName());
                    urlVideoRequest.setExpiration(expirationDate);
                    urlVideoRequest.setResponseHeaders(override);

                    URL videoUrl = s3Client.generatePresignedUrl(urlVideoRequest);

                    override.setContentType("image/jpeg");
                    GeneratePresignedUrlRequest urlImageRequest = new GeneratePresignedUrlRequest(AppEnvironment.getInstance().PICTURE_BUCKET, uploadParams.getThumbnailName());
                    urlImageRequest.setExpiration(expirationDate);
                    urlImageRequest.setResponseHeaders(override);

                    // LogUtil.i("Creating Request: " +
                    // uploadParams.getFileName());

                    URL imageUrl = s3Client.generatePresignedUrl(urlImageRequest);

                    // LogUtil.i("Calling URLS " + uploadParams.getFileName());

                    result.setUri(Uri.parse(videoUrl.toURI().toString()));

                    uploadParams.mVideo.setFileUrl(videoUrl.toURI().toString());
                    uploadParams.mVideo.setThumbUrl(imageUrl.toURI().toString());

                    // LogUtil.i("VID: " + videoUrl.toURI().toString());
                    // LogUtil.i("IMG: " + imageUrl.toURI().toString());

                    // updateTextView.obtainMessage(VIDEO_SENT).sendToTarget();

                } catch (Exception exception) {

                    LogUtil.e(exception.getMessage());
                }
            }

            return result;
        }

        protected void onPostExecute(S3TaskResult result) {

            if (result.getErrorMessage() != null) {

                // displayErrorAlert("There was a failure",
                // result.getErrorMessage());
            } else if (result.getUri() != null) {

                // Display in Browser.
                // startActivity(new Intent(Intent.ACTION_VIEW,
                // result.getUri()));

                Log.i("Upload", "Uploaded to: " + result.getUri().toString());
                // Toast.makeText(getApplicationContext(),
                // "Uploaded successfully", 2000).show();

                // TestPostTask task = new TestPostTask();
                // task.execute(new String[] { result.getUri().toString() });
            }
            LogUtil.i("Video params stuff like yo");
            Intent intent = new Intent(IABIntent.GET_URLS);
            IABroadcastManager.sendLocalBroadcast(intent);
        }
    }

    // TODO: Abstract this crappy piece of shit way of video cancelling
    S3DownloadTask downloadTask;

    public void downloadS3(String bucketName, String pictureId) {
        // S3Object object = s3Client.getObject(bucketName, pictureId);
        // object.getObjectContent();

        if (downloadTask != null) {
            LogUtil.e("Attempting to cancel task");
            downloadTask.cancel(true);
            downloadTask = null;
        }

        downloadTask = new S3DownloadTask();
        downloadTask.execute(new GetObjectRequest(bucketName, pictureId));

    }

    /**
     * this was a custom class that was built because the system can not accept
     * Braodcast based architucture to constantly change videos.
     */
    S3DownloadTaskWithVideo downloadWithVideoTask;

    public void downloadS3(String bucketName, String pictureId, ProgressHelper progresshelper, CustomVideoView targetVideoPlayer, View wrapperInformation, ArrayList<CustomVideoView> videos) {

        if (downloadWithVideoTask != null) {
            LogUtil.e("Attempting to cancel task");
            downloadWithVideoTask.cancel(true);
            downloadWithVideoTask = null;
        }

        downloadWithVideoTask = new S3DownloadTaskWithVideo(targetVideoPlayer, videos, wrapperInformation);
        downloadWithVideoTask.execute(new GetObjectRequest(bucketName, pictureId));

    }

    Long contentLength = 0L;

    private class S3DownloadTaskWithVideo extends AsyncTask<GetObjectRequest, Long, String> {
        CustomVideoView mTargetVideoPlayer = null;
        ArrayList<CustomVideoView> mVideos;
        View mWrapperInformation;

        ProgressHelper mProgresshelper;

        public S3DownloadTaskWithVideo(CustomVideoView videoplayer, ArrayList<CustomVideoView> videos, View wrapperInformation) {
            mWrapperInformation = wrapperInformation;
            mVideos = videos;
            mTargetVideoPlayer = videoplayer;
            mProgresshelper = mTargetVideoPlayer.getProgressHelper();
        }

        // From AsyncTask, run on UI thread before execution
        protected void onPreExecute() {
            // stopDownButton.setClickable(true);
            // startDownButton.setClickable(false);
        }

        // From AsyncTask
        protected String doInBackground(GetObjectRequest... reqs) {
            byte buffer[] = new byte[1024];
            S3ObjectInputStream is;
            // write the inputStream to a FileOutputStream
            FileOutputStream outputStream;

            String request = reqs[0].getKey();

            try {
                contentLength = s3Client.getObject(reqs[0]).getObjectMetadata().getContentLength();
                LogUtil.i("Content Length: " + contentLength + " Request: " + request);
                is = s3Client.getObject(reqs[0]).getObjectContent();

                outputStream = new FileOutputStream(HBFileUtil.getOutputVideoFile(reqs[0].getKey()));

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Long totalRead = 0L;
            int bytesRead = 1;
            try {
                while ((bytesRead > 0) && (!this.isCancelled())) {
                    bytesRead = is.read(buffer);
                    if (buffer.length > 0 && bytesRead > 0) {
                        LogUtil.d("QUIT WRITE");
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    totalRead += bytesRead;
                    publishProgress(totalRead);
                }

                // abort the get object request
                if (this.isCancelled()) {
                    is.abort();
                }

                // close our stream
                outputStream.close();
                is.close();

                if (!this.isCancelled()) {

                    /*
                     * Intent intent = new Intent(IABIntent.INTENT_REQUEST_VIDEO); intent.putExtra(IABIntent.PARAM_ID, request); IABroadcastManager.sendLocalBroadcast(intent);
                     * LogUtil.i("broadcast Sent");
                     */
                    return request;

                } else {
                    LogUtil.e("Task Cancelled");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

            return "";
        }

        // From AsyncTask, runs on UI thread when background calls
        // publishProgress
        protected void onProgressUpdate(Long... progress) {
            // Toast.makeText(HollerbackApplication.getInstance(),
            // progress[0].toString(), 700).show();

            LogUtil.i("Progress: " + progress[0].toString() + " / " + contentLength);

            if (mOnProgressListener != null) {
                mOnProgressListener.onProgress(progress[0], contentLength);
            }

            // LogUtil.i("Progress: " + (progress[0] * 100 / contentLength) +
            // "%");

        }

        // From AsyncTask, runs on UI thread when background calls
        // publishProgress
        protected void onPostExecute(final String request) {
            // downloadAmount.setText("DONE! " + result);
            // stopDownButton.setClickable(false);
            // startDownButton.setClickable(true);
            if (mOnProgressListener != null) {
                mOnProgressListener.onComplete();
            }
            if (!request.equalsIgnoreCase("")) {
                for (int i = 0; i < mVideos.size(); i++) {
                    if (mVideos.get(i).hasProgressHelper()) {
                        mVideos.get(i).stopProgressHelper();
                    }
                    if (mVideos.get(i).isPlaying()) {
                        mVideos.get(i).pause();
                        mVideos.get(i).stopPlayback();
                    }
                    mVideos.get(i).setVisibility(View.GONE);
                    mVideos.remove(i);
                }
                String path = HBFileUtil.getLocalFile(request);
                mTargetVideoPlayer.setVisibility(View.VISIBLE);
                mTargetVideoPlayer.setVideoPath(path);
                mTargetVideoPlayer.requestFocus();
                mProgresshelper.hideLoader();
                mTargetVideoPlayer.start();
                mTargetVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mWrapperInformation.setVisibility(View.GONE);
                        mTargetVideoPlayer.setVisibility(View.GONE);
                        mVideos.remove(mTargetVideoPlayer);

                        if (mTargetVideoPlayer.hasNextView()) {
                            mTargetVideoPlayer.getNextView().performClick();
                        }

                        if (mTargetVideoPlayer.hasBlowupParentView()) {
                            mTargetVideoPlayer.getBlowupParentView().performClick();
                        }
                    }
                });
                mVideos.add(mTargetVideoPlayer);
            }
        }

        // From AsyncTask, runs on UI thread called when task is canceled from
        // any other thread
        protected void onCancelled() {
            // stopDownButton.setClickable(false);
            // startDownButton.setClickable(true);
        }
    }

    private class S3DownloadTask extends AsyncTask<GetObjectRequest, Long, Long> {

        // From AsyncTask, run on UI thread before execution
        protected void onPreExecute() {
            // stopDownButton.setClickable(true);
            // startDownButton.setClickable(false);
        }

        // From AsyncTask
        protected Long doInBackground(GetObjectRequest... reqs) {
            byte buffer[] = new byte[1024];
            S3ObjectInputStream is;
            // write the inputStream to a FileOutputStream
            FileOutputStream outputStream;

            String request = reqs[0].getKey();

            try {
                contentLength = s3Client.getObject(reqs[0]).getObjectMetadata().getContentLength();
                LogUtil.i("Content Length: " + contentLength + " Request: " + request);
                is = s3Client.getObject(reqs[0]).getObjectContent();

                outputStream = new FileOutputStream(HBFileUtil.getOutputVideoFile(reqs[0].getKey()));

            } catch (Exception e) {
                e.printStackTrace();
                return 0L;
            }
            Long totalRead = 0L;
            int bytesRead = 1;
            try {
                while ((bytesRead > 0) && (!this.isCancelled())) {
                    bytesRead = is.read(buffer);
                    if (buffer.length > 0 && bytesRead > 0) {
                        LogUtil.d("QUIT WRITE");
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    totalRead += bytesRead;
                    publishProgress(totalRead);
                }

                // abort the get object request
                if (this.isCancelled()) {
                    is.abort();
                }

                // close our stream
                outputStream.close();
                is.close();

                if (!this.isCancelled()) {

                    Intent intent = new Intent(IABIntent.REQUEST_VIDEO);
                    intent.putExtra(IABIntent.PARAM_ID, request);
                    IABroadcastManager.sendLocalBroadcast(intent);
                    LogUtil.i("broadcast Sent");
                } else {
                    LogUtil.e("Task Cancelled");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return 0L;
            }

            return totalRead;
        }

        // From AsyncTask, runs on UI thread when background calls
        // publishProgress
        protected void onProgressUpdate(Long... progress) {
            // Toast.makeText(HollerbackApplication.getInstance(),
            // progress[0].toString(), 700).show();

            LogUtil.i("Progress: " + progress[0].toString() + " / " + contentLength);

            if (mOnProgressListener != null) {
                mOnProgressListener.onProgress(progress[0], contentLength);
            }

            // LogUtil.i("Progress: " + (progress[0] * 100 / contentLength) +
            // "%");

        }

        // From AsyncTask, runs on UI thread when background calls
        // publishProgress
        protected void onPostExecute(Long result) {
            // downloadAmount.setText("DONE! " + result);
            // stopDownButton.setClickable(false);
            // startDownButton.setClickable(true);
            if (mOnProgressListener != null) {
                mOnProgressListener.onComplete();
            }
        }

        // From AsyncTask, runs on UI thread called when task is canceled from
        // any other thread
        protected void onCancelled() {
            // stopDownButton.setClickable(false);
            // startDownButton.setClickable(true);
        }
    }

    private static class S3TaskResult {
        String errorMessage = null;
        Uri uri = null;
        S3UploadParams uploadParams;

        public void setS3UploadParams(S3UploadParams params) {
            uploadParams = params;
        }

        public S3UploadParams getS3UploadParams() {
            return uploadParams;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }
    }

    public static class S3UploadParams {

        private Context mContext;
        private OnS3UploadListener mOnS3UploadListener;
        private String mFilePath;
        private String mFileName;
        private String mFileType;
        public VideoModel mVideo;
        public String conversationId;

        public String customMessage;

        public String getFileType() {
            return mFileType;
        }

        public void setFileType(String mFileType) {
            this.mFileType = mFileType;
        }

        public static String VID_MP4 = "mp4";
        public static String IMG_PNG = "-thumb.png";

        public static String CONTENT_TYPE_MP4 = "video/mp4";
        public static String CONTENT_TYPE_PNG = "image/png";

        public Context getmContext() {
            return mContext;
        }

        public void setContext(Context mContext) {
            this.mContext = mContext;
        }

        public OnS3UploadListener getOnS3UploadListener() {
            return mOnS3UploadListener;
        }

        public void setOnS3UploadListener(OnS3UploadListener mOnS3UploadListener) {
            this.mOnS3UploadListener = mOnS3UploadListener;
        }

        public String getFilePath() {
            return mFilePath;
        }

        public void setFilePath(String mFilePath) {
            this.mFilePath = mFilePath;
        }

        public String getFileName() {
            return mFileName;
        }

        public void setFileName(String mFileName) {
            this.mFileName = mFileName;
        }

        public String getVideoName() {
            return mFileName;

        }

        public String getThumbnailName() {
            return HBFileUtil.getImageUploadName(mFileName);
        }
    }
}
