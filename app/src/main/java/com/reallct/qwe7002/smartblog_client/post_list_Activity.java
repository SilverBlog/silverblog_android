package com.reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class post_list_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshWidget;
    get_post_list_content get_post_list_content_exec;
    ArrayList<String> title_list;
    private MyReceiver receiver;
    private Context context;
    private int tab_position = 0;
    private static final String MY_BROADCAST_TAG = "com.reallct.qwe7002.smartblog_client";
    ArrayList<Integer> list_position;
    String share_title = null;
    String share_text = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_list_menu, menu);
        return true;
    }

    void handleSendText(Intent intent) {
        share_title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        share_text = intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_post);
        context = getApplicationContext();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MY_BROADCAST_TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("文章列表");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new push_to_git().execute();
                return false;
            }
        });
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent new_post_activity = new Intent(post_list_Activity.this, post_Activity.class);
                new_post_activity.putExtra("share_title", share_title);
                new_post_activity.putExtra("share_text", share_text);
                share_text = null;
                share_title = null;
                startActivity(new_post_activity);
            }
        });
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent();
                intent.setAction(MY_BROADCAST_TAG);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        listView = (ListView) findViewById(R.id.edit_post_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(tab_position==1){
                    start_post_activity(position);
                    return;

                }
                new AlertDialog.Builder(post_list_Activity.this).setTitle("请选择操作").setItems(new String[]{"修改", "删除"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                start_post_activity(position);
                                break;
                            case 1:
                                new AlertDialog.Builder(post_list_Activity.this).setTitle("删除这篇文章？(警告！本操作不可逆，请谨慎操作！）").setPositiveButton("确定",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                new delete_post().execute(Integer.toString(list_position.get(position)), title_list.get(list_position.get(position)));
                                            }
                                        }).setNegativeButton("取消", null).show();
                                break;
                        }
                    }
                }).show();
            }
        });
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        Intent Broadcast_intent = new Intent();
        Broadcast_intent.setAction(MY_BROADCAST_TAG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(Broadcast_intent);
        TabLayout tab = (TabLayout) findViewById(R.id.tab_layout2);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab_position != tab.getPosition()) {
                    tab_position = tab.getPosition();
                    listView.setAdapter(null);
                    Intent intent = new Intent();
                    intent.setAction(MY_BROADCAST_TAG);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
                fab.setVisibility(View.VISIBLE);
                if(tab_position==1){
                    fab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void start_post_activity(int position) {
        Intent intent = new Intent(post_list_Activity.this, post_Activity.class);
        intent.putExtra("edit", true);
        intent.putExtra("position", list_position.get(position));
        intent.putExtra("share_title", share_title);
        intent.putExtra("share_text", share_text);
        if (tab_position == 1) {
            intent.putExtra("menu", true);
        }
        share_text = null;
        share_title = null;
        startActivity(intent);
    }
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            get_post_list_content_exec = new get_post_list_content();
            get_post_list_content_exec.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        get_post_list_content_exec.cancel(true);
    }

    private class push_to_git extends AsyncTask<Void, Integer, String> {
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
        protected String doInBackground(Void... args) {
            String url = sharedPreferences.getString("host", null);
            return request.send_request(url, "{}", "git_page_publish");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            String result_message = "操作失败！请查看API服务器输出！";
            if (objects.get("status").getAsBoolean()) {
                result_message = "操作完成！";
            }
            Snackbar.make(findViewById(R.id.fab), result_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    private class get_post_list_content extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            mSwipeRefreshWidget.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Integer... args) {
            String url = sharedPreferences.getString("host", null);
            String mode = "{}";
            String active_name = "get_post_list";
            if (tab_position == 1) {
                active_name = "get_menu_list";
            }
            return request.send_request(url, mode, active_name);
        }

        @Override
        protected void onPostExecute(String result) {
            mSwipeRefreshWidget.setRefreshing(false);
            JsonParser parser = new JsonParser();
            if (parser.parse(result).isJsonArray()) {
                final JsonArray result_array = parser.parse(result).getAsJsonArray();
                title_list = new ArrayList<>();
                list_position = new ArrayList<>();
                ArrayList<String> time_list = new ArrayList<>();
                int for_i = 0;
                for (JsonElement item : result_array) {
                    JsonObject sub_item = item.getAsJsonObject();
                    Boolean add_switch = true;
                    //检查是否为绝对路径
                    if (sub_item.has("absolute") && sub_item.get("absolute").getAsBoolean()) {
                        add_switch = false;
                    }
                    if (sub_item.get("name").getAsString().equals("index") || sub_item.get("name").getAsString().equals("/")){
                        add_switch=false;
                    }
                    if (add_switch) {
                        title_list.add(sub_item.get("title").getAsString());
                        String time = "";
                        if (sub_item.has("time")) {
                            time = sub_item.get("time").getAsString();
                        }
                        list_position.add(for_i);
                        time_list.add(time);
                    }
                    for_i++;
                }

                if (title_list.size() == 0) {
                    Snackbar.make(mSwipeRefreshWidget, "当前文章列表为空", Snackbar.LENGTH_LONG).show();
                }

                List<HashMap<String, String>> list = new ArrayList<>();
                for (int i = 0; i < title_list.size(); i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("title", title_list.get(i));
                    hashMap.put("time", time_list.get(i));
                    list.add(hashMap);
                }
                ListAdapter adapter = new SimpleAdapter(
                        post_list_Activity.this,
                        list,
                        android.R.layout.simple_list_item_2,
                        new String[]{"title", "time"},
                        new int[]{android.R.id.text1, android.R.id.text2});
                listView.setAdapter(adapter);
            } else {
                Snackbar.make(findViewById(R.id.fab), "获取文章列表失败，请检查网络状态！", Snackbar.LENGTH_LONG).show();
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
            return request.send_request(url, "{\"post_id\":" + args[0] + ",\"encode\":\"" + request.getMD5(args[0] + args[1] + password) + "\"}", "delete");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            String result_message = "操作失败！请检查服务器地址以及API密码。";
            if (objects.get("status").getAsBoolean()) {
                result_message = "操作完成！";
            }
            Snackbar.make(findViewById(R.id.fab), result_message, Snackbar.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction(MY_BROADCAST_TAG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
