package com.moziy.hollerback.util;

import android.widget.Toast;

import com.moziy.hollerback.HollerbackApplication;

/**
 * Todo: make code time based..?
 * @author sajjad
 *
 */
public class HBEasterEggUtil {

    private static final short EASTER_EGG_MODE = 0x67;
    private static final short COPY_DB_TO_SDCARD = 0x1C;

    private static boolean inEasterEggMode = false;

    public interface Preferences {

    }

    public static void init() {
        inEasterEggMode = false;
        sXcode = 0;
        sYcode = 0;
    }

    private static short sXcode;
    private static short sYcode;

    public static void setX(boolean set) {
        sXcode <<= 1; // shift left by 1
        if (set) {
            sXcode++; // add 1
            setY(false);
            evaluate();
        }

    }

    public static void setY(boolean set) {
        sYcode <<= 1;
        if (set) {
            sYcode++;
            setX(false);
            evaluate();
        }

    }

    public static void evaluate() {
        if (!inEasterEggMode) {
            if (sXcode == EASTER_EGG_MODE && isMatch(sXcode, sYcode)) {
                inEasterEggMode = true;
                sXcode = 0;
                sYcode = 0;
                Toast.makeText(HollerbackApplication.getInstance(), "Muhahaha, ...", Toast.LENGTH_LONG).show();
            }
        } else {
            if (sXcode == COPY_DB_TO_SDCARD && isMatch(sXcode, sYcode)) {
                DBUtil.copyDbToSdcard();
                sXcode = 0;
                sYcode = 0;
            }

        }
    }

    private static boolean isMatch(short x, short y) {
        return (y == (~x & y));
    }
}
