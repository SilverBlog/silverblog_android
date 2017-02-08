package reallct.qwe7002.smartblog_client;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;


/**
 * Created by qwe70 on 2017/2/9.
 */

public class API {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static void sendnewpost(String url,String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        http.Send(request);
    }
}
