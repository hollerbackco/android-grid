package com.moziy.hollerback.camera.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.moziy.hollerback.debug.LogUtil;

public class CameraUtil {
    private static final String TAG = CameraUtil.class.getSimpleName();
    private static final int KBPS = 1000;
    public static final int AUDIO_SAMPLE_RATE = 32 * KBPS;
    public static final int AUDIO_ENCODING_BIT_RATE = 96 * KBPS;
    public static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    public static final int VIDEO_ENCODING_RATE = 256 * KBPS;
    public static final int VIDEO_FRAME_RATE = 24;
    public static final int VIDEO_OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4;
    public static final int VIDEO_OUTPUT_ENCODER = MediaRecorder.VideoEncoder.H264;

    public static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        boolean isSquare = false;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            LogUtil.i("Preview: w:" + size.width + " h:" + size.height);

            if (size.width <= width && size.height <= height) {
                if (!isSquare) {
                    if (result == null) {
                        result = size;
                    } else {
                        int resultArea = result.width * result.height;
                        int newArea = size.width * size.height;
                        if (newArea > resultArea) {
                            result = size;
                        }
                    }
                }
            }

            if (size.width == size.height) {
                isSquare = true;
                if (result == null) {
                    result = size;
                }
                // if the current size is already square
                if (isSquare) {
                    // we only take smaller, otherwise ignore
                    if (size.width <= result.width) {
                        result = size;
                    }
                } else
                    result = size;
            }

        }
        return (result);
    }

    public static Size getPreferredSize(List<Size> supportedVideoSizes, int preferredWidth, int preferredHeight) {
        Size actual = null;

        if (supportedVideoSizes == null) {
            return null;
        }

        // sort them in descending order
        Collections.sort(supportedVideoSizes, new Comparator<Size>() {

            @Override
            public int compare(Size lhs, Size rhs) {
                if (lhs.width > rhs.width) {
                    return 1;
                } else if (lhs.width < rhs.width) {
                    return -1;
                } else {
                    // lets compare the heights
                    if (lhs.height > rhs.height) {
                        return 1;
                    } else if (lhs.height < rhs.height) {
                        return -1;
                    }
                }

                return 0;
            }

        });

        Collections.reverse(supportedVideoSizes);

        for (Size s : supportedVideoSizes) {

            if (s.height == preferredHeight && s.width == preferredWidth) { // if we found a match then lets note it
                Log.d(TAG, "getPreferredSize - returning preferred");
                return s; // we found a match
            }

            if (actual == null) {
                actual = s;
            } else {
                if (s.width < actual.width && s.width > preferredWidth) { // lets find the best video size closest to the preferred
                    Log.d(TAG, "getPreferredSize() - actual: " + actual.width + " " + actual.height);
                    actual = s;
                }
            }

        }

        return actual;
    }

    /**
     * This method returns the optimal preview size for a given width and height. 
     * The method imposes an aspect ratio tollerence of 0.1
     * @param sizes
     * @param w
     * @param h
     * @return The optimal preview size
     */
    public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static boolean HasSquareCamera(Camera.Parameters parameters) {
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width == size.height) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public static int setRecordingParams(MediaRecorder recorder, int width, int height) {

        if (Build.VERSION.SDK_INT >= 15) { // if the device has this profile, then use it

            if (CamcorderProfile.hasProfile(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_QVGA)) {
                CamcorderProfile qvga = CamcorderProfile.get(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_QVGA);
                recorder.setProfile(qvga);
                Log.d(TAG, "setting qvga profile");
                return qvga.fileFormat;
            }
        }

        // video
        recorder.setOutputFormat(VIDEO_OUTPUT_FORMAT);
        // recorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        recorder.setVideoSize(width, height);
        recorder.setVideoEncodingBitRate(VIDEO_ENCODING_RATE);
        recorder.setVideoEncoder(VIDEO_OUTPUT_ENCODER);

        // audio
        recorder.setAudioEncodingBitRate(AUDIO_ENCODING_BIT_RATE);
        recorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE);
        recorder.setAudioEncoder(AUDIO_ENCODER);

        return VIDEO_OUTPUT_FORMAT;
    }

    public static void printAllCamcorderProfiles(int cameraId) {

        Field[] fields = CamcorderProfile.class.getFields();
        for (Field f : fields) {
            try {

                if (Modifier.isStatic(f.getModifiers()) && CamcorderProfile.hasProfile(cameraId, f.getInt(f))) {
                    Log.d(TAG, f.getName() + " found");
                    CamcorderProfile profile = CamcorderProfile.get(cameraId, f.getInt(f));

                    Field[] objectFields = profile.getClass().getFields();
                    StringBuilder sb = new StringBuilder();
                    for (Field pfield : objectFields) {

                        if (!Modifier.isStatic(pfield.getModifiers())) {
                            sb.append(pfield.getName()).append(": ").append(pfield.get(profile)).append("\n");
                        }
                    }
                    Log.d(TAG, "profile: " + (sb != null ? sb.toString() : ""));

                } else if (Modifier.isStatic(f.getModifiers())) {
                    Log.d(TAG, f.getName() + " not found");
                }
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
