package reallct.qwe7002.smartblog_client;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import static android.content.ContentValues.TAG;


/**
 * Created by qwe70 on 2017/2/9.
 */

public class API {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static String sendnewpost(String url,String json) {
        Log.d(TAG, "sendnewpost: "+json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url+"/newpost")
                .post(body)
                .build();
        return http.Send(request);
    }
}
