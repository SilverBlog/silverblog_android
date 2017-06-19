package reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class post_list_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshWidget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("文章列表");
        setSupportActionBar(toolbar);
        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshWidget.setRefreshing(true);
                new get_post_list_content().execute();
            }
        });
        listView = (ListView) findViewById(R.id.edit_post_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int click_location = position + 1;
                Intent intent = new Intent(post_list_Activity.this, edit_post_Activity.class);
                startActivityForResult(intent, click_location);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //// TODO: 2017/6/19 长按删除功能
                Snackbar.make(view, "你长按的是第" + (position + 1) + "条数据", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
        });
        new get_post_list_content().execute();
    }

    private class get_post_list_content extends AsyncTask<Void, Integer, String> {

        ProgressDialog mpDialog = new ProgressDialog(post_list_Activity.this);

        @Override
        protected void onPreExecute() {
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle("正在连接服务器...");
            mpDialog.setMessage("正在获取数据，请稍后...");
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
        }

        @Override
        protected String doInBackground(Void... args) {
            String password = sharedPreferences.getString("password", null);
            String url = sharedPreferences.getString("host", null);
            return API.send_request(url,"{\"encode\":\""+API.getMD5("get_post_list"+password)+"\"}", "get_post_list");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            mSwipeRefreshWidget.setRefreshing(false);
            JsonParser parser = new JsonParser();
            final JsonArray result_array = parser.parse(result).getAsJsonArray();
            ArrayList<JsonElement> list = new ArrayList<>();
            for (JsonElement item : result_array) {
                list.add(item);
            }
            ListAdapter adapter = new ArrayAdapter<>(post_list_Activity.this, android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
        }
    }

}
