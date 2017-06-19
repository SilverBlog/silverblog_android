package reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
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
                Intent intent = new Intent(post_list_Activity.this, edit_post_Activity.class);
                startActivityForResult(intent, position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_list_Activity.this);
                alertDialog.setTitle("删除这篇文章？(警告！本操作不可逆，请谨慎操作！）");
                alertDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new delete_post().execute(Integer.toString(position));
                            }
                        });
                return false;
            }
        });
        new get_post_list_content().execute();
    }

    private class get_post_list_content extends AsyncTask<Void, Integer, String> {


        @Override
        protected void onPreExecute() {
            mSwipeRefreshWidget.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... args) {
            String url = sharedPreferences.getString("host", null);
            return API.send_request(url, "{}", "get_post_list");
        }

        @Override
        protected void onPostExecute(String result) {
            mSwipeRefreshWidget.setRefreshing(false);
            JsonParser parser = new JsonParser();
            if (parser.parse(result).isJsonArray()) {
                final JsonArray result_array = parser.parse(result).getAsJsonArray();
                ArrayList<JsonElement> list = new ArrayList<>();
                for (JsonElement item : result_array) {
                    list.add(item);
                }
                ListAdapter adapter = new ArrayAdapter<>(post_list_Activity.this, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_list_Activity.this);
                alertDialog.setTitle("操作失败！请检查服务器配置及网络连接。");
                alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                alertDialog.show();
            }

        }
    }
    private class delete_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(post_list_Activity.this);

        @Override
        protected void onPreExecute() {
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle("正在连接服务器...");
            mpDialog.setMessage("正在提交数据，请稍后...");
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            String url = sharedPreferences.getString("host", null);
            String password = sharedPreferences.getString("password", null);
            return API.send_request(url,"{\"post_id\":"+args[0]+",\"encode\":\""+API.getMD5(args[0]+password)+"\"}","delete");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_list_Activity.this);
            alertDialog.setTitle("操作失败！请检查服务器地址以及API密码。");
            if (objects.get("status").getAsBoolean()) {
                alertDialog.setTitle("操作完成！");
            }
            alertDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            alertDialog.create().show();
        }
    }

}
