package com.moziy.hollerback.helper;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.krish.horizontalscrollview.CustomListBaseAdapter;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.video.S3UploadParams;
import com.moziy.hollerback.view.CustomVideoView;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnProgressListener;
import com.moziy.hollerbacky.connection.RequestCallbacks.OnS3UploadListener;

//TODO: Abstract the upload methods, verification and buckets

public class S3RequestHelper {

	private static AmazonS3Client s3Client;

	private static OnProgressListener mOnProgressListener;

	public S3RequestHelper() {
		if (s3Client == null) {
			s3Client = new AmazonS3Client(new BasicAWSCredentials(
					AppEnvironment.getInstance().ACCESS_KEY_ID,
					AppEnvironment.getInstance().SECRET_KEY));
		}

	}

	public String uploadFile(S3UploadParams params, String filePath) {
		return null;
	}

	public void registerOnProgressListener(OnProgressListener onProgressListener) {
		mOnProgressListener = onProgressListener;
	}

	public void clearOnProgressListener() {
		mOnProgressListener = null;
	}

	public void uploadNewVideo(final String conversationId,
			final String videoName, String imageName, String customMessage,
			OnS3UploadListener onS3UploadListener) {
		S3UploadParams video = new S3UploadParams();
		S3UploadParams thumb = new S3UploadParams();

		video.setFileName(videoName);
		video.setFilePath(FileUtil.getLocalFile(videoName));

		contentLength = FileUtil.getFileSize(videoName);
		video.conversationId = conversationId;
		thumb.setFileName(imageName);
		thumb.setFilePath(FileUtil.getLocalFile(imageName));
		video.customMessage = customMessage;

		video.setOnS3UploadListener(onS3UploadListener);

		S3PutObjectTask s3task = new S3PutObjectTask();
		s3task.execute(new S3UploadParams[] { video, thumb });

	}

	private class S3PutObjectTask extends
			AsyncTask<S3UploadParams, Void, S3TaskResult> {

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		protected void onPreExecute() {

		}

		protected S3TaskResult doInBackground(S3UploadParams... videos) {

			S3UploadParams videoRequestParam = videos[0];
			S3UploadParams thumbRequestParam = videos[1];

			S3TaskResult result = new S3TaskResult();
			result.uploadParams = videoRequestParam;

			videoRequestParam.getOnS3UploadListener().onStart();

			// Put the image data into S3.
			try {
				// s3Client.createBucket(AppEnvironment.getPictureBucket());

				// Content type is determined by file extension.
				PutObjectRequest videoUploadRequest = new PutObjectRequest(
						AppEnvironment.getInstance().UPLOAD_BUCKET,
						videoRequestParam.getFileName(), new java.io.File(
								videoRequestParam.getFilePath()));

				PutObjectRequest thumbUploadRequest = new PutObjectRequest(
						AppEnvironment.getInstance().UPLOAD_BUCKET,
						thumbRequestParam.getFileName(), new java.io.File(
								thumbRequestParam.getFilePath()));

				videoUploadRequest.setProgressListener(new ProgressListener() {

					@Override
					public void progressChanged(ProgressEvent arg0) {
						if (mOnProgressListener != null) {
							mOnProgressListener.onProgress(
									arg0.getBytesTransfered(), contentLength);
						}
					}
				});

				s3Client.putObject(thumbUploadRequest);
				s3Client.putObject(videoUploadRequest);

			} catch (Exception exception) {

				result.setErrorMessage(exception.getMessage());
				exception.printStackTrace();
			}

			return result;
		}

		protected void onPostExecute(S3TaskResult result) {

			if (result.getErrorMessage() != null) {

				// displayErrorAlert("Upload Failure",
				// result.getErrorMessage());

				LogUtil.e(result.getErrorMessage());

			} else {

				if (result.getS3UploadParams() != null) {
					result.getS3UploadParams().getOnS3UploadListener()
							.onComplete();
				}

				if (AppEnvironment.getInstance().ALLOW_UPLOAD_VIDEOS) {
					HBRequestManager.postVideo(
							result.getS3UploadParams().conversationId, result
									.getS3UploadParams().getFileName(), result
									.getS3UploadParams().customMessage);
					LogUtil.i("LOL CATWALK");
				}
			}

			if (result != null) {
				//
				// if(result.getS3UploadParams()!=null){
				// result.getS3UploadParams().getOnS3UploadListener()
				// .onS3Upload(true);
				// }

				// Toast.makeText(getApplicationContext(),
				// "Uploaded to: " + result.getUri().toString(),
				// Toast.LENGTH_LONG).show();
				// Toast.makeText(getApplicationContext(),
				// "Uploaded to: " + result.getUri().getPath(),
				// Toast.LENGTH_LONG).show();

			}

			// new S3GeneratePresignedUrlTask()
			// .execute(new S3UploadParams[] { result.getS3UploadParams() });
		}
	}

	public void getS3URLParams(ArrayList<S3UploadParams> videos) {
		S3UploadParams[] videosArray = videos.toArray(new S3UploadParams[videos
				.size()]);
		new S3RequestHelper.S3GenerateUrlTask().execute(videosArray);
	}

	public class S3GenerateUrlTask extends
			AsyncTask<S3UploadParams, Void, S3TaskResult> {

		protected S3TaskResult doInBackground(S3UploadParams... videos) {

			S3TaskResult result = new S3TaskResult();
			for (S3UploadParams uploadParams : videos) {
				try {
					LogUtil.i("S3GenTask for " + uploadParams.getFileName());
					// Ensure that the image will be treated as such.
					ResponseHeaderOverrides override = new ResponseHeaderOverrides();

					override.setContentType("video/mp4");
					Date expirationDate = new Date(
							System.currentTimeMillis() + 3600000);

					GeneratePresignedUrlRequest urlVideoRequest = new GeneratePresignedUrlRequest(
							AppEnvironment.getInstance().PICTURE_BUCKET,
							uploadParams.getFileName());
					urlVideoRequest.setExpiration(expirationDate);
					urlVideoRequest.setResponseHeaders(override);

					URL videoUrl = s3Client
							.generatePresignedUrl(urlVideoRequest);

					override.setContentType("image/jpeg");
					GeneratePresignedUrlRequest urlImageRequest = new GeneratePresignedUrlRequest(
							AppEnvironment.getInstance().PICTURE_BUCKET,
							uploadParams.getThumbnailName());
					urlImageRequest.setExpiration(expirationDate);
					urlImageRequest.setResponseHeaders(override);

					// LogUtil.i("Creating Request: " +
					// uploadParams.getFileName());

					URL imageUrl = s3Client
							.generatePresignedUrl(urlImageRequest);

					// LogUtil.i("Calling URLS " + uploadParams.getFileName());

					result.setUri(Uri.parse(videoUrl.toURI().toString()));

					uploadParams.mVideo.setFileUrl(videoUrl.toURI().toString());
					uploadParams.mVideo
							.setThumbUrl(imageUrl.toURI().toString());

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
			Intent intent = new Intent(IABIntent.INTENT_GET_URLS);
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
	public void downloadS3(String bucketName, 
			String pictureId, 
			ProgressHelper progresshelper,
			CustomVideoView targetVideoPlayer,
			View wrapperInformation,
			ArrayList<CustomVideoView> videos) {

		if (downloadWithVideoTask != null) {
			LogUtil.e("Attempting to cancel task");
			downloadWithVideoTask.cancel(true);
			downloadWithVideoTask = null;
		}

		downloadWithVideoTask = new S3DownloadTaskWithVideo(targetVideoPlayer, videos, wrapperInformation);
		downloadWithVideoTask.execute(new GetObjectRequest(bucketName, pictureId));

	}
	

	Long contentLength = 0L;
	
	private class S3DownloadTaskWithVideo extends
			AsyncTask<GetObjectRequest, Long, String> {
		CustomVideoView mTargetVideoPlayer = null;
		ArrayList<CustomVideoView> mVideos;
		View mWrapperInformation;
		
		ProgressHelper mProgresshelper;
		public S3DownloadTaskWithVideo(CustomVideoView videoplayer, ArrayList<CustomVideoView> videos, View wrapperInformation)
		{ 
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
				contentLength = s3Client.getObject(reqs[0]).getObjectMetadata()
						.getContentLength();
				LogUtil.i("Content Length: " + contentLength + " Request: "
						+ request);
				is = s3Client.getObject(reqs[0]).getObjectContent();
		
				outputStream = new FileOutputStream(
						FileUtil.getOutputVideoFile(reqs[0].getKey()));
		
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
					Intent intent = new Intent(IABIntent.INTENT_REQUEST_VIDEO);
					intent.putExtra(IABIntent.PARAM_ID, request);
					IABroadcastManager.sendLocalBroadcast(intent);
					LogUtil.i("broadcast Sent");
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
		
			LogUtil.i("Progress: " + progress[0].toString() + " / "
					+ contentLength);
		
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
			if(!request.equalsIgnoreCase(""))
			{
				for(int i = 0; i < mVideos.size(); i++)
				{
					if(mVideos.get(i).hasProgressHelper())
					{
						mVideos.get(i).stopProgressHelper();
					}
					if(mVideos.get(i).isPlaying())
					{
						mVideos.get(i).pause();
						mVideos.get(i).stopPlayback();
					}
					mVideos.get(i).setVisibility(View.GONE);
					mVideos.remove(i);
				}
				String path = FileUtil.getLocalFile(request);
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
						
						if(mTargetVideoPlayer.hasNextView())
						{
							mTargetVideoPlayer.getNextView().performClick();
						}
						
						if(mTargetVideoPlayer.hasBlowupParentView())
						{
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

	private class S3DownloadTask extends
			AsyncTask<GetObjectRequest, Long, Long> {

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
				contentLength = s3Client.getObject(reqs[0]).getObjectMetadata()
						.getContentLength();
				LogUtil.i("Content Length: " + contentLength + " Request: "
						+ request);
				is = s3Client.getObject(reqs[0]).getObjectContent();

				outputStream = new FileOutputStream(
						FileUtil.getOutputVideoFile(reqs[0].getKey()));

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

					Intent intent = new Intent(IABIntent.INTENT_REQUEST_VIDEO);
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

			LogUtil.i("Progress: " + progress[0].toString() + " / "
					+ contentLength);

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

	private class S3TaskResult {
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

}
