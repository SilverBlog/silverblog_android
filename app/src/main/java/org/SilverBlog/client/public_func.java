package org.SilverBlog.client;


import java.nio.charset.StandardCharsets;


class public_func {

    static String getMD5(String source) {
        String mdString = null;
        if (source != null) {
            mdString = getBytes(source.getBytes(StandardCharsets.UTF_8));
        }
        return mdString;
    }

    private static String getBytes(byte[] source) {
        String s = null;
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        final int temp = 0xf;
        final int arraySize = 32;
        final int strLen = 16;
        final int offset = 4;
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            md.update(source);
            byte[] tmp = md.digest();
            char[] str = new char[arraySize];
            int k = 0;
            for (int i = 0; i < strLen; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> offset & temp];
                str[k++] = hexDigits[byte0 & temp];
            }
            s = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
