package com.reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class post_Activity extends AppCompatActivity {
    EditText titleview;
    EditText nameview;
    EditText editTextview;
    SharedPreferences sharedPreferences;
    int request_post_id;
    String action_name = "new";
    private Context context;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_toolbar_menu, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (titleview.getText().length() == 0 || editTextview.getText().length() == 0) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                alertDialog.setTitle("标题和内容不能为空！");
                alertDialog.setNegativeButton("确定", null);
                alertDialog.show();
                return false;
            }
            switch (menuItem.getItemId()) {
                case R.id.send_post_button:
                    String password = sharedPreferences.getString("password", null);
                    if (password != null) {
                        Gson gson = new Gson();
                        content_json content = new content_json();
                        content.setName(nameview.getText().toString());
                        content.setTitle(titleview.getText().toString());
                        content.setContent(editTextview.getText().toString());
                        content.setEncode(api.getMD5(titleview.getText().toString() + password));
                        String json = gson.toJson(content);
                        new push_post().execute(json);
                    }
                    break;
                case R.id.send_edit_app_button:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, editTextview.getText().toString());
                    intent.putExtra(Intent.EXTRA_SUBJECT, titleview.getText().toString());
                    intent.setType("text/plain");
                    startActivity(intent);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        titleview = (EditText) findViewById(R.id.titleview);
        editTextview = (EditText) findViewById(R.id.mdcontent);
        nameview = (EditText) findViewById(R.id.nameview);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        this.setTitle("发布文章");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        Intent intent = getIntent();
        titleview.setText(intent.getStringExtra("share_title"));
        editTextview.setText(intent.getStringExtra("share_text"));
        if (intent.getBooleanExtra("edit", false)) {
            action_name = "edit";
            this.setTitle("修改文章");
            request_post_id = intent.getIntExtra("position", -1);
            new get_post_content().execute(Integer.toString(request_post_id));
        }
    }


    private class get_post_content extends AsyncTask<String, Integer, String> {

        ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);

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
        protected String doInBackground(String... args) {
            String url = sharedPreferences.getString("host", null);
            return api.send_request(url, "{\"post_id\":" + args[0] + "}", "get_post_content");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            if (objects.get("status").getAsBoolean()) {
                if (editTextview.getText().length() == 0) {
                    titleview.setText(objects.get("title").getAsString());
                    editTextview.setText(objects.get("content").getAsString());
                }
                nameview.setText(objects.get("name").getAsString());
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                alertDialog.setTitle("操作失败！请检查服务器配置及网络连接。");
                alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            }
        }
    }

    private class content_json {
        private int post_id;
        private String content;
        private String encode;
        private String title;
        private String name;

        void setName(String name) {
            this.name = name;
        }

        void setPost_id(int post_id) {
            this.post_id = post_id;
        }

        void setTitle(String title) {
            this.title = title;
        }

        void setContent(String Content) {
            this.content = Content;
        }

        void setEncode(String Encode) {
            this.encode = Encode;
        }
    }

    private class push_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);

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
            return api.send_request(url, args[0], action_name);
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
            alertDialog.setTitle("操作失败！请检查服务器地址以及API密码。");
            String ok_button = "确定";
            if (objects.get("status").getAsBoolean()) {
                alertDialog.setTitle("操作完成！");
                ok_button = "访问新博文";
                alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction("com.reallct.qwe7002.smartblog_client");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        finish();
                    }
                });
            }
            alertDialog.setPositiveButton(ok_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (objects.get("status").getAsBoolean()) {
                                Uri uri = Uri.parse(sharedPreferences.getString("host", null) + "/" + objects.get("name").getAsString());
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            }
                            Intent intent = new Intent();
                            intent.setAction("com.reallct.qwe7002.smartblog_client");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            finish();
                        }
                    });
            alertDialog.create().show();
        }
    }
}
