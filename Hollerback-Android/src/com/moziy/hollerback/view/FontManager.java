package com.moziy.hollerback.view;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;

import com.moziy.hollerback.HollerbackApplication;

public class FontManager {

    private static class FileName {
        public static final String GOTHAM_MEDIUM = "Gotham-Medium.otf";
        public static final String RALEWAY_REGULAR_LINING = "Raleway-Regular-Lining.ttf";
        public static final String GOTHAM_ROUND_MEDIUM = "GothamRnd-Medium.otf";
    }

    public interface Font {
        public static final String GOTHAM_MEDIUM = "Gotham-Medium";
        public static final String RALEWAY_REGULAR_LINING = "Raleway-Regular-Lining";
        public static final String GOTHAM_ROUND_MEDIUM = "GothamRnd-Medium";
    }

    public static Map<String, Typeface> sFontFileMap;
    private static final String FONT_PATH = "fonts/%s";

    static {
        sFontFileMap = new HashMap<String, Typeface>();
        sFontFileMap.put(Font.GOTHAM_MEDIUM, Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, FileName.GOTHAM_MEDIUM)));
        sFontFileMap.put(Font.RALEWAY_REGULAR_LINING, Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, FileName.RALEWAY_REGULAR_LINING)));
        sFontFileMap.put(Font.GOTHAM_ROUND_MEDIUM, Typeface.createFromAsset(HollerbackApplication.getInstance().getAssets(), String.format(FONT_PATH, FileName.GOTHAM_ROUND_MEDIUM)));
    }

    public static Typeface getFont(String fontName) {

        if (fontName == null || "".equals(fontName)) {
            return null;
        }

        Typeface font = sFontFileMap.get(fontName);
        return font;
    }
}
