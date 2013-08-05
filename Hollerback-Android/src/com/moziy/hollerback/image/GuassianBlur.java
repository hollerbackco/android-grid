package com.moziy.hollerback.image;

import android.graphics.Bitmap;

public class GuassianBlur implements IAndroidFilter {

	@Override
	public AndroidImage process(AndroidImage imageIn) {
		// TODO Auto-generated method stub
		Bitmap src = imageIn.getImage();
		double[][] GaussianBlurConfig = new double[][] { { 1, 2, 1 },
				{ 2, 4, 2 }, { 1, 2, 1 } };
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(GaussianBlurConfig);
		convMatrix.Factor = 200;
		convMatrix.Offset = 0;
		return new AndroidImage(ConvolutionMatrix.computeConvolution3x3(src,
				convMatrix));
	}

}