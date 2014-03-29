package com.moziy.hollerback.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moziy.hollerback.HollerbackApplication;

/**
 * Access a lot of things in the app stands for quickutil
 * 
 * @author jianchen
 * 
 */
public class QU {

    public static ObjectMapper getObjectMapper() {
        return HollerbackApplication.getInstance().getObjectMapper();
    }

    /**
     * Get String from strings file
     */
    public static String s(int id) {
        return HollerbackApplication.getInstance().getString(id);
    }

}
