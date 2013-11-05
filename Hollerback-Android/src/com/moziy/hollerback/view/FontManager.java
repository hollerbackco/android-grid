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

    public static Map<String, Typeface> sFontFileMap;
    private static final String FONT_PATH = "fonts/%s";

    static {
        sFontFileMap = new HashMap<String, Typeface>();
        sFontFileMap.put(Font.GOTHAM_MEDIUM, Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, FileName.GOTHAM_MEDIUM)));
        sFontFileMap.put(Font.RALEWAY_REGULAR_LINING, Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, FileName.RALEWAY_REGULAR_LINING)));
    }

    public static Typeface getFont(String fontName) {

        if (fontName == null || "".equals(fontName)) {
            return null;
        }

        Typeface font = sFontFileMap.get(fontName);

        Log.d("fontmanager", "setting font");
        return font;
    }
}
