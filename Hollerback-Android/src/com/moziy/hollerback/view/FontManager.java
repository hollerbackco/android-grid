package com.moziy.hollerback.view;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;
import android.util.Log;

import com.moziy.hollerback.HollerbackApplication;

public class FontManager {

    private static class FileName {
        public static final String GOTHAM_MEDIUM = "Gotham-Medium.otf";
        public static final String RALEWAY_REGULAR_LINING = "Raleway-Regular-Lining.ttf";
    }

    public interface Font {
        public static final String GOTHAM_MEDIUM = "Gotham-Medium";
        public static final String RALEWAY_REGULAR_LINING = "Raleway-Regular-Lining";
    }

    public static Map<String, String> sFontFileMap;

    static {
        sFontFileMap = new HashMap<String, String>();
        sFontFileMap.put(Font.GOTHAM_MEDIUM, FileName.GOTHAM_MEDIUM);
        sFontFileMap.put(Font.RALEWAY_REGULAR_LINING, FileName.RALEWAY_REGULAR_LINING);

    }

    private static final String FONT_PATH = "fonts/%s";

    public static Typeface getFont(String font) {
        if (font == null || "".equals(font)) {
            return null;
        }

        String fontName = sFontFileMap.get(font);
        if (fontName == null) {
            return null;
        }
        Log.d("fontmanager", "setting font");
        return Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, fontName));

    }
}
