package com.moziy.hollerback.util;

import android.hardware.Camera;
import android.media.MediaRecorder;

import com.moziy.hollerback.debug.LogUtil;

public class CameraUtil {

    public static final int AUDIO_SAMPLE_RATE = 32000;
    public static final int AUDIO_ENCODING_BIT_RATE = 96000;
    public static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    public static final int VIDEO_ENCODING_RATE = 512000;
    public static final int VIDEO_OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4;
    public static final int VIDEO_OUTPUT_ENCODER = MediaRecorder.VideoEncoder.MPEG_4_SP;

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

}
