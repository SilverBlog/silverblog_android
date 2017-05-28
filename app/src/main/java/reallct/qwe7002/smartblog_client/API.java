package reallct.qwe7002.smartblog_client;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


/**
 * Created by qwe70 on 2017/2/9.
 */

class API {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    static String sendnewpost(String url, String json){
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url+"/control/new")
                .post(body)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        String result= "{\"status\":false}";
        try {
            Response response = okHttpClient.newCall(request).execute();
            result=response.body().string();
        } catch (IOException ignored) {
        }
        return result;
    }
}
