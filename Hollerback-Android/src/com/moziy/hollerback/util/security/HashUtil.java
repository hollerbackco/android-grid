package com.moziy.hollerback.util.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.moziy.hollerback.communication.IABIntent;

public class HashUtil {

    private static MessageDigest MD5;
    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
            MD5.reset();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param value
     * @return 128bit value at 4 bytes per char (length 32)
     */
    public static String getMD5(String value) {
        try {
            MD5.update(value.getBytes(), 0, value.length());
            String encoding = new BigInteger(1, MD5.digest()).toString(16);
            MD5.reset();
            return encoding;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 
     * @param content the byte array for the content to generate the md5
     * @return the Hex string representing the md5
     */
    public static String generateHexStringMD5(byte[] content) {

        byte[] hash = MD5.digest(content);
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * On average SHA256 is about 40 times faster than using MD5
     * 
     * @param value
     * @return 256bit value at 4 bytes per char (length 64)
     */
    public static String getSHA256(String value) {
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(value.getBytes(), 0, value.length());
            String encoding = new BigInteger(1, m.digest()).toString(16);
            return encoding;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDefaultHash(String value) {
        return getSHA256(value);
    }

    public static String generateHashFor(String s, String s1) {
        String hash = Integer.toString(s.hashCode()) + Integer.toString(s1.hashCode());
        HashUtil.getSHA256(hash);

        return hash;
    }

    public static String getConvHash() {
        return HashUtil.generateHashFor(IABIntent.GET_CONVERSATIONS, IABIntent.VALUE_CONV_HASH);
    }
}
