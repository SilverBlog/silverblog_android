package reallct.qwe7002.smartblog_client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = sharedPreferences.getString("password", null);
                Gson gson = new Gson();
                contentjson content = new contentjson();
                content.setTitle(titleview.getText().toString());
                content.setContent(editTextview.getText().toString());
                content.setEncode(md5(titleview + password));
                String json = gson.toJson(content);
                new pushpost().execute(json);
            }
        });
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // 处理发送来的文字
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

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public class contentjson {
        private String title;
        private String content;
        private String encode;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String Content) {
            this.content = Content;
        }

        public void setEncode(String Encode) {
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

    public class pushpost extends AsyncTask<String, Integer, String> {
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewPostActivity.this);
            alertDialog
                    .setTitle("操作完成！")
                    .setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .create()
                    .show();

        }
    }
}
