package reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class edit_post_Activity extends AppCompatActivity {
    EditText titleview;
    EditText editTextview;
    SharedPreferences sharedPreferences;
    int request_post_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        titleview = (EditText) findViewById(R.id.title);
        editTextview = (EditText) findViewById(R.id.mdcontent);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        this.setTitle("修改文章");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String password = sharedPreferences.getString("password", null);
                if (password != null) {
                    String json = "{\"post_id\":\""+Integer.toString(request_post_id)+"\",\"title\":\"" + titleview.getText().toString() + "\",\"content\":\"" + editTextview.getText().toString() + "\",\"encode\":\"" + API.getMD5(titleview.getText().toString() + password) + "\"}";
                    new push_post().execute(json);
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        request_post_id = requestCode;
        new get_post_content().execute(Integer.toString(requestCode));
    }

    private class get_post_content extends AsyncTask<String, Integer, String> {

        ProgressDialog mpDialog = new ProgressDialog(edit_post_Activity.this);

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
            return API.send_request(url, args[0], "new");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            //// TODO: 2017/6/19 文本信息
        }
    }

    private class push_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(edit_post_Activity.this);

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
            return API.send_request(url, args[0], "edit");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_post_Activity.this);
            alertDialog.setTitle("操作失败！请检查服务器地址以及API密码。");
            String okbutton = "确定";
            if (objects.get("status").getAsBoolean()) {
                alertDialog.setTitle("操作完成！");
                okbutton = "访问新博文";
                alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            }
            alertDialog.setPositiveButton(okbutton,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (objects.get("status").getAsBoolean()) {
                                Uri uri = Uri.parse(sharedPreferences.getString("host", null) + "/" + objects.get("name").getAsString());
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            }
                            finish();
                        }
                    });
            alertDialog.create().show();
        }
    }
}
