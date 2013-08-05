package com.moziy.hollerbacky.connection;

/**
 * Collection of Interfaces
 * @author jianchen
 *
 */
public class RequestCallbacks {

	public interface OnS3UploadListener {

		public void onStart();

		public int onProgress(long progress);

		public int onComplete();

		public void onS3Upload(boolean success);

		public void onS3Url(String url, boolean success);
	}

	public interface OnProgressListener {
		public void onProgress(long amount, long total);

		public void onComplete();

	}

	public interface OnGCMReceivedListener {
		public void onGCMReceived(String token);
	}
}
