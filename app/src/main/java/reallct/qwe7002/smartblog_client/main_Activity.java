package reallct.qwe7002.smartblog_client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.zxing.activity.CaptureActivity;


public class main_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    String host_save;
    String password_save;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (ContextCompat.checkSelfPermission(main_Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(main_Activity.this, new String[]{Manifest.permission.CAMERA}, 1);
                    return false;
                }
                Intent intent = new Intent(main_Activity.this, CaptureActivity.class);
                startActivityForResult(intent, 0);
                return true;
            }
        });
        Button save_button = (Button) findViewById(R.id.save_button);
        Button new_post_button = (Button) findViewById(R.id.send_button);
        Button edit_post_button = (Button) findViewById(R.id.edit_button);
        final EditText host = (EditText) findViewById(R.id.host);
        final EditText password = (EditText) findViewById(R.id.password);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        host_save = sharedPreferences.getString("host", null);
        password_save = sharedPreferences.getString("password", null);
        host.setText(host_save);
        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        new_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host_save != null && password_save != null) {
                    Intent new_post_activity = new Intent(main_Activity.this, post_Activity.class);
                    startActivity(new_post_activity);
                    return;
                }
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        edit_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host_save != null && password_save != null) {
                    Intent edit_post_activity = new Intent(main_Activity.this, post_list_Activity.class);
                    startActivity(edit_post_activity);
                    return;
                }
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (host.getText().length() == 0 && password.getText().length() == 0) {
                    Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                String hosturl = String.valueOf(host.getText());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("host", String.valueOf(hosturl));
                editor.putString("password", silverblog_connect.getMD5(String.valueOf(password.getText())));
                editor.apply();
                host_save = String.valueOf(hosturl);
                password_save = silverblog_connect.getMD5(String.valueOf(password.getText()));
                Snackbar.make(view, "配置已保存", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(main_Activity.this, CaptureActivity.class);
                    startActivityForResult(intent, 0);
                    return;
                }
                Toast.makeText(this, "请授权相机权限，以便能够正常扫描二维码。", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            JsonParser parser = new JsonParser();
            JsonObject objects;
            try {
                 objects = parser.parse(scanResult).getAsJsonObject();
            }catch(JsonParseException e){
                Toast.makeText(this,"二维码错误！",Toast.LENGTH_LONG).show();
                return;
            }
            String hosturl = String.valueOf(objects.get("url").getAsString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("host", String.valueOf(hosturl));
            editor.putString("password", silverblog_connect.getMD5(objects.get("password").getAsString()));
            editor.apply();
            host_save = String.valueOf(hosturl);

        }
    }
}
