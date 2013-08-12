package com.moziy.hollerback.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.HollerbackInterfaces.OnFilterVideoListener;
import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.RecordVideoFragment.FilterAdapter;
import com.moziy.hollerback.fragment.RecordVideoFragment.FilterFragment;
import com.moziy.hollerback.util.CameraHelper;
import com.moziy.hollerback.util.CameraHelper.CameraInfo2;
import com.moziy.hollerback.util.GPUImageFilterTools;
import com.moziy.hollerback.util.GPUImageFilterTools.FilterAdjuster;
import com.moziy.hollerback.util.GPUImageFilterTools.FilterList;
import com.moziy.hollerback.util.GPUImageFilterTools.OnGpuImageFilterChosenListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class GPURecordVideoFragment extends BaseFragment implements OnClickListener{
	private ViewGroup mRootView;
	private SherlockFragmentActivity mActivity;
	
    private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    private GPUImageFilter mFilter;
    private FilterAdjuster mFilterAdjuster;
    
    private static FilterList mFilterList;
	private ViewPager mFilterPagers;
	private OnFilterVideoListener mFilterVideoListener;
    
	public static GPURecordVideoFragment newInstance() {
		GPURecordVideoFragment f = new GPURecordVideoFragment();
		return f;
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_camera, null);
		mActivity = (SherlockFragmentActivity)this.getActivity();
		
        mRootView.findViewById(R.id.button_choose_filter).setOnClickListener(this);
        mRootView.findViewById(R.id.button_capture).setOnClickListener(this);

        mGPUImage = new GPUImage(mActivity);
        mGPUImage.setGLSurfaceView((GLSurfaceView) mRootView.findViewById(R.id.surfaceView));

        mCameraHelper = new CameraHelper(mActivity);
        mCamera = new CameraLoader();

        View cameraSwitchView = mRootView.findViewById(R.id.img_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }

        mFilterList = GPUImageFilterTools.getFilters();
        
        mFilterVideoListener = new OnFilterVideoListener(){

			@Override
			public void onFilterSelected(int position) {
	            switchFilterTo(GPUImageFilterTools.createFilterForType(mActivity, mFilterList.filters.get(position)));
			}
        	
        };
        
		mFilterPagers = (ViewPager)mRootView.findViewById(R.id.filters);
		mFilterPagers.setAdapter(new FilterAdapter(mActivity.getSupportFragmentManager(), mFilterVideoListener));
        
        return mRootView;
	}
	
    @Override
	public void onResume() {
        super.onResume();
        mCamera.onResume();
    }

    @Override
	public void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_choose_filter:
                GPUImageFilterTools.showDialog(mActivity, new OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                    }
                });
                break;

            case R.id.button_capture:
                if (mCamera.mCameraInstance.getParameters().getFocusMode().equals(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    takePicture();
                } else {
                    mCamera.mCameraInstance.autoFocus(new Camera.AutoFocusCallback() {

                        @Override
                        public void onAutoFocus(final boolean success, final Camera camera) {
                            takePicture();
                        }
                    });
                }
                break;

            case R.id.img_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    private void takePicture() {
        // TODO get a size that is about the size of the screen
        Camera.Parameters params = mCamera.mCameraInstance.getParameters();
        params.setPictureSize(1280, 960);
        params.setRotation(90);
        mCamera.mCameraInstance.setParameters(params);
        for (Camera.Size size2 : mCamera.mCameraInstance.getParameters()
                .getSupportedPictureSizes()) {
            Log.i("ASDF", "Supported: " + size2.width + "x" + size2.height);
        }
        mCamera.mCameraInstance.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {

                        final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            Log.d("ASDF",
                                    "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("ASDF", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("ASDF", "Error accessing file: " + e.getMessage());
                        }

                        data = null;
                        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile
                                .getAbsolutePath());
                        // mGPUImage.setImage(bitmap);
                        final GLSurfaceView view = (GLSurfaceView) mRootView.findViewById(R.id.surfaceView);
                        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        mGPUImage.saveToPictures(bitmap, "GPUImage",
                                System.currentTimeMillis() + ".jpg",
                                new OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri
                                            uri) {
                                        pictureFile.delete();
                                        camera.startPreview();
                                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                    }
                                });
                    }
                });
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static File getOutputMediaFile(final int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            mFilterAdjuster.adjust(95);
        }
    }

    private class CameraLoader {
        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            parameters.setPreviewSize(720, 480);
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
            		mActivity, mCurrentCameraId);
            CameraInfo2 cameraInfo = new CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT
                    ? true : false;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }

	@Override
	protected void initializeView(View view) {
		// TODO Auto-generated method stub
		
	}
	
    public class FilterAdapter extends FragmentPagerAdapter {
    	OnFilterVideoListener mListener;
        public FilterAdapter(FragmentManager fm, OnFilterVideoListener listener) {
            super(fm);
            mListener = listener; 
        }

        @Override
        public int getCount() {
            return mFilterList.names.size();
        }

        @Override
        public Fragment getItem(int position) {
            return FilterFragment.newInstance(position, mListener);
        }
    }
    

    public static class FilterFragment extends Fragment {
        int mNum;
        private static OnFilterVideoListener mListener;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static FilterFragment newInstance(int num, OnFilterVideoListener listener) {
        	mListener = listener;
        	FilterFragment f = new FilterFragment();
            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);
            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.text_filter, container, false);
            mListener.onFilterSelected(mNum);

            View tv = v.findViewById(R.id.txtFilter);
            ((TextView)tv).setText(mFilterList.names.get(mNum));
            return v;
        }
    }
}
