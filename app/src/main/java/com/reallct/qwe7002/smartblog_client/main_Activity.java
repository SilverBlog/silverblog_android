package com.reallct.qwe7002.smartblog_client;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.activity.CaptureActivity;


public class main_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    String host_save;
    String password_save;
    EditText host;
    EditText password;

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
        Button edit_post_button = (Button) findViewById(R.id.edit_button);
        host = (EditText) findViewById(R.id.host);
        password = (EditText) findViewById(R.id.password);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        host_save = sharedPreferences.getString("host", null);
        password_save = sharedPreferences.getString("password", null);
        if (password_save != null) {
            password.setText("{\"password_seted\"}");
        }
        host.setText(host_save);
        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        edit_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host_save != null) {
                    Intent edit_post_activity = new Intent(main_Activity.this, post_list_Activity.class);
                    startActivity(edit_post_activity);
                    return;
                }
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                Snackbar.make(view, R.string.check_input, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (host.getText().length() == 0 || password.getText().toString().equals("{\"password_seted\"}") || password.getText().length() == 0) {
                    Snackbar.make(view, R.string.check_input, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                String host_url = String.valueOf(host.getText());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("host", host_url);
                editor.putString("password", request.getMD5(String.valueOf(password.getText())));
                editor.apply();
                host_save = String.valueOf(host_url);
                password.setText("{\"password_seted\"}");
                Snackbar.make(view, R.string.config_save, Snackbar.LENGTH_LONG)
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
                Snackbar.make(host, R.string.scan_qr, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            JsonParser parser = new JsonParser();
            JsonObject objects;
            try {
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
            editor.putString("host", host_save);
            editor.putString("password", password_save);
            editor.apply();
            host.setText(host_save);
            password.setText("{\"password_seted\"}");
            Snackbar.make(host, R.string.config_save, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }
}
