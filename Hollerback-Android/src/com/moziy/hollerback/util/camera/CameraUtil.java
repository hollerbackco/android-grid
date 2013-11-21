package com.moziy.hollerback.util.camera;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;

import com.moziy.hollerback.debug.LogUtil;

public class CameraUtil {
    private static final String TAG = CameraUtil.class.getSimpleName();
    private static final int KBPS = 1000;
    public static final int AUDIO_SAMPLE_RATE = 32 * KBPS;
    public static final int AUDIO_ENCODING_BIT_RATE = 96 * KBPS;
    public static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    public static final int VIDEO_ENCODING_RATE = 500 * KBPS;
    public static final int VIDEO_FRAME_RATE = 30;
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

    public static Size getPreferredVideoSize(int preferredWidth, int preferredHeight, Camera.Parameters camParams) {
        Size actual = null;

        List<Size> supportedVideoSizes = camParams.getSupportedVideoSizes();

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

        for (Size s : supportedVideoSizes) {

            if (s.height == preferredHeight && s.width == preferredWidth) { // if we found a match then lets note it
                Log.d(TAG, "getPreferredVideoSize - returning preferred");
                return s; // we found a match
            }

            if (actual == null) {
                actual = s;
            } else {
                if (s.width < actual.width && s.width > preferredWidth) { // lets find the best video size closest to the preferred
                    Log.d(TAG, "getPreferredVideoSize() - actual: " + actual.width + " " + actual.height);
                    actual = s;
                }
            }

        }

        return actual;
    }

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

    public static void setFrontFacingParams(MediaRecorder recorder, int width, int height) {
        recorder.setOutputFormat(VIDEO_OUTPUT_FORMAT);

        recorder.setVideoEncoder(VIDEO_OUTPUT_ENCODER);
        recorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE);
        recorder.setAudioEncodingBitRate(AUDIO_ENCODING_BIT_RATE);
        recorder.setVideoEncodingBitRate(VIDEO_ENCODING_RATE);
        recorder.setAudioEncoder(AUDIO_ENCODER);

        recorder.setVideoSize(width, height);
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
}
