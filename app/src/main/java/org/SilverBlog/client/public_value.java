package org.SilverBlog.client;

import com.google.gson.JsonArray;

import okhttp3.MediaType;
import okhttp3.RequestBody;

class public_value {
    static String host;
    static String password;
    static String share_title = null;
    static String share_text = null;
    static JsonArray post_list;
    static JsonArray menu_list;
    static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    static final String MY_BROADCAST_TAG = "org.silverblog.client";
    static final RequestBody nullbody = RequestBody.create(null, new byte[0]);
}
