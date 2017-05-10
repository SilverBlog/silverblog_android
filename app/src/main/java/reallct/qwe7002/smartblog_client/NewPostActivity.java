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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;

public class NewPostActivity extends AppCompatActivity {
    EditText titleview;
    EditText editTextview;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        titleview = (EditText) findViewById(R.id.title);
        editTextview = (EditText) findViewById(R.id.mdcontent);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        this.setTitle("发布文章");
        Button send = (Button) findViewById(R.id.button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = sharedPreferences.getString("password", null);
                Gson gson = new Gson();
                content_json content = new content_json();
                content.setTitle(titleview.getText().toString());
                content.setContent(editTextview.getText().toString());
                content.setEncode(getMD5(titleview.getText().toString() + password));
                String json = gson.toJson(content);
                new push_post().execute(json);
            }
        });
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            titleview.setText(sharedTitle);
            editTextview.setText(sharedText);
        }
    }


    public static String getMD5(String source) {
        String mdString = null;
        if (source != null) {
            try {
                mdString = getBytes(source.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return mdString;
    }

    public static String getBytes(byte[] source) {
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

    private class content_json {
        private String title;
        private String content;
        private String encode;

        public void setTitle(String title) {
            this.title = title;
        }

        void setContent(String Content) {
            this.content = Content;
        }

        void setEncode(String Encode) {
            this.encode = Encode;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getEncode() {
            return encode;
        }
    }

    private class push_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(NewPostActivity.this);

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
            return API.sendnewpost(url, args[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewPostActivity.this);
            alertDialog.setTitle("操作失败！");
            if (objects.get("status").getAsBoolean()) {
                alertDialog.setTitle("操作完成！");
            }
            alertDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(objects.get("status").getAsBoolean()){
                                Uri uri = Uri.parse(sharedPreferences.getString("host", null)+"/"+objects.get("name").getAsString());
                                startActivity(new Intent(Intent.ACTION_VIEW,uri));
                            }
                            finish();
                        }
                    })
                    .create()
                    .show();
        }
    }
}
