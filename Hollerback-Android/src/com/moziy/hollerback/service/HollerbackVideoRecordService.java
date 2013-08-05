package com.moziy.hollerback.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Patch Fix for the SegFaults caused by having more than 1 thread open in
 * FFMPEG
 * 
 * @author jianchen
 * 
 */
public class HollerbackVideoRecordService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
