package com.moziy.hollerback.service;

import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.helper.S3RequestHelper;
import com.moziy.hollerback.video.S3UploadParams;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class HollerbackVideoService extends IntentService {

	/**
	 * Main each connection for up to 120 seconds
	 */
	private static long MAX_UPLOAD_TIMEOUT = 120000;
	private static long MAX_NUMBER_RETRY = 3;

	public HollerbackVideoService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (IABIntent.isIntent(intent, IABIntent.INTENT_SERVICE_UPLOADVIDEO)) {
			String path = intent.getStringExtra(IABIntent.PARAM_VIDEO_PATH);
			S3UploadParams s3uploadParam = new S3UploadParams();
			uploadVideoService(s3uploadParam);
		}

	}

	public void uploadVideoService(S3UploadParams param) {
		S3RequestHelper helper = new S3RequestHelper();
		helper.uploadNewVideo(param.conversationId, param.getFilePath(),
				param.getThumbnailName(), "", null);
	}

	public void startRetryVideoService() {
		
	}

}
