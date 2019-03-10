package org.SilverBlog.client;

import com.google.gson.JsonArray;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;


class public_func {

    public static String get_hmac_hash(String base, String key, String type) {
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac;
        try {
            mac = Mac.getInstance(type);
            mac.init(secret);
            byte[] digest = mac.doFinal(base.getBytes());
            return byte2hex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String byte2hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            int v = aB & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }


    static String get_hash(String source, String func) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(func);
            byte[] digest = md.digest(source.getBytes());
            return byte2hex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}

class public_value {
    final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    final static String API_VERSION = "v2";
    static String host;
    static String password;
    static Boolean init = false;
    static String share_title = null;
    static String share_text = null;
    static JsonArray post_list;
    static JsonArray menu_list;
}
