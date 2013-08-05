package com.moziy.hollerback.util;

import com.moziy.hollerback.HollerbackApplication;

import android.graphics.Typeface;

public class FontUtil {
	
	public final static Typeface MuseoSans_500 = Typeface.createFromAsset(
			HollerbackApplication.getInstance().getApplicationContext()
					.getAssets(), "fonts/MuseoSans_500.otf");
}
