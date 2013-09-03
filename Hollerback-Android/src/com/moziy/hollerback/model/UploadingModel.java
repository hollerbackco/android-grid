package com.moziy.hollerback.model;

import android.widget.TextView;

import com.moziy.hollerback.helper.ProgressHelper;

public class UploadingModel {
	private ProgressHelper progressHelper;
	private TextView txtSent;
	private VideoModel videoModel;
	
	public ProgressHelper getProgressHelper()
	{
		return progressHelper;
	}
	
	public TextView getTxtSent()
	{
		return txtSent;		
	}
	
	public VideoModel getVideoModel()
	{
		return videoModel;
	}
	
	public void setProgressHelper(ProgressHelper value)
	{
		progressHelper = value;
	}
	
	public void setTxtSent(TextView value)
	{
		txtSent = value;
	}
	
	public void setVideoModel(VideoModel value)
	{
		videoModel = value;
	}
}
