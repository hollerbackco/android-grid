package com.moziy.hollerback.view;

import com.moziy.hollerback.helper.ProgressHelper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;
    private ProgressHelper mProgresshelper;
    private View mNextView;
    private View mBlowupParentView;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mForceWidth, mForceHeight);
    }
    
    public void changeVideoSize(int width, int height)
    {
    	mForceWidth = width;       
        mForceHeight = height;

        // not sure whether it is useful or not but safe to do so
        getHolder().setFixedSize(width, height); 
        
        requestLayout();
        invalidate();     // very important, so that onMeasure will be triggered
    } 
    
    public void setProgressHelper(ProgressHelper progress)
    {
    	mProgresshelper = progress;
    }
    
    public ProgressHelper getProgressHelper()
    {
    	return mProgresshelper;
    }
    
    public void stopProgressHelper()
    {
    	mProgresshelper.hideLoader();
    }
    
    public boolean hasProgressHelper()
    {
    	if(mProgresshelper != null)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    public void setNextView(View view)
    {
    	mNextView = view;
    }
    
    public View getNextView()
    {
    	return mNextView;
    }
    
    public boolean hasNextView()
    {
    	if(mNextView != null)
    	{
    		return true;
    	}
    	return false;
    }
    
    public void setBlowupParentView(View view)
    {
    	mBlowupParentView = view;
    }
    
    public View getBlowupParentView()
    {
    	return mBlowupParentView;
    }
    
    public boolean hasBlowupParentView()
    {
    	if(mBlowupParentView != null)
    	{
    		return true;
    	}
    	return false;
    }
}