package org.SilverBlog.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.activity.CaptureActivity;

import org.silverblog.client.R;

import java.util.ArrayList;
import java.util.Map;


public class main_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    String host_save;
    String password_save;
    EditText host;
    EditText password;
    JsonObject host_list;
    ArrayList<String> host_name_list;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.maintoolbar);
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

        host_list = new JsonParser().parse(sharedPreferences.getString("host_list", "{}")).getAsJsonObject();
        host_name_list = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : host_list.entrySet()) {
            host_name_list.add(entry.getKey());
        }
        Button old_button = findViewById(R.id.use_old);
        old_button.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              new AlertDialog.Builder(view.getContext()).setTitle(R.string.select_config).setItems(host_name_list.toArray(new String[0]), new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                      JsonObject host_info = host_list.get(host_name_list.get(i)).getAsJsonObject();
                                                      host_save = host_info.get("host").getAsString();
                                                      password_save = host_info.get("password").getAsString();
                                                      SharedPreferences.Editor editor = sharedPreferences.edit();
                                                      editor.putString("host", host_save);
                                                      editor.putString("password", password_save);
                                                      editor.apply();
                                                      start_edit();

                                                  }
                                              }).show();
                                          }
                                      }
        );
        Button save_button = findViewById(R.id.save_button);
        host = findViewById(R.id.host);
        password = findViewById(R.id.password);
        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                if (host.getText().length() == 0 || password.getText().length() == 0) {
                    Snackbar.make(view, R.string.check_input, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                host_save = String.valueOf(host.getText());
                password_save = request.getMD5(String.valueOf(password.getText()));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (!host_list.has(host_save)) {
                    host_list.add(host_save, new JsonParser().parse("{\"host\":\"" + host_save + "\",\"password\":\"" + password_save + "\"}"));
                }
                editor.putString("host_list", new Gson().toJson(host_list));
                editor.putString("host", host_save);
                editor.putString("password", password_save);
                editor.apply();
                start_edit();

            }
        });
    }

    void start_edit() {
        public_value.host = host_save;
        public_value.password = password_save;
        Intent edit_post_activity = new Intent(main_Activity.this, post_list_card_Activity.class);
        startActivity(edit_post_activity);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode ==1){
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(main_Activity.this, CaptureActivity.class);
                    startActivityForResult(intent, 0);
                    return;
                }
                Snackbar.make(host, R.string.scan_qr, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = null;
            if (bundle != null) {
                scanResult = bundle.getString("result");
            }
            JsonParser parser = new JsonParser();
            JsonObject objects;
            try {
                assert scanResult != null;
                objects = parser.parse(scanResult).getAsJsonObject();
            } catch (IllegalStateException e) {
                Snackbar.make(host, R.string.QRcode_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            host_save = objects.get("url").getAsString();
            password_save = objects.get("password").getAsString();
            if (password_save.length() == 0 || host_save.length() == 0) {
                Snackbar.make(host, R.string.check_config, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (!host_list.has(host_save)) {
                host_list.add(host_save, new JsonParser().parse("{\"host\":\"" + host_save + "\",\"password\":\"" + password_save + "\"}"));
            }
            editor.putString("host_list", new Gson().toJson(host_list));
            editor.putString("host", host_save);
            editor.putString("password", password_save);
            editor.apply();
            start_edit();
        }
    }
}
