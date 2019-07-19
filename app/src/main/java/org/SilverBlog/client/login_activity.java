package org.SilverBlog.client;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class login_activity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String host_save;
    private String password_save;
    private EditText host;
    private EditText password;
    private JsonObject host_list;
    private ArrayList<String> host_name_list;
    private Context context;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    private final View.OnClickListener history_host = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(view.getContext()).setTitle(R.string.select_config).setItems(host_name_list.toArray(new String[0]), (dialogInterface, index) -> {
                JsonObject host_info = host_list.get(host_name_list.get(index)).getAsJsonObject();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                host_save = host_info.get("host").getAsString();
                if (host_info.has("password")) {
                    String password_v2 = public_func.get_hmac_hash(host_info.get("password").getAsString(), final_value.public_passwd_key, "HmacSHA256");
                    host_list.remove(host_name_list.get(index));
                    JsonObject object = new JsonObject();
                    object.addProperty("host", host_save);
                    object.addProperty("password_v2", password_v2);
                    host_list.add(host_name_list.get(index), object);
                    editor.putString("host_list", new Gson().toJson(host_list));
                    password_save = password_v2;
                } else {
                    password_save = host_info.get("password_v2").getAsString();
                }
                editor.putString("host", host_save);
                editor.putString("password_v2", password_save);
                editor.apply();
                start_edit();

            }).setNegativeButton(R.string.clean, (dialogInterface, i) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("host_list");
                editor.remove("host");
                editor.remove("password_v2");
                editor.apply();
                host_list = new JsonParser().parse("{}").getAsJsonObject();
                host_name_list = new ArrayList<>();
            }).setPositiveButton(R.string.cancel, null).show();
        }
    };
    private final View.OnClickListener save_host = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
            password_save = public_func.get_hmac_hash(Objects.requireNonNull(public_func.get_hash(String.valueOf(password.getText()), "MD5")), final_value.public_passwd_key, "HmacSHA256");
            final ProgressDialog progress_dialog = new ProgressDialog(login_activity.this);
            progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress_dialog.setTitle(getString(R.string.connecting));
            progress_dialog.setMessage(getString(R.string.connecting_message));
            progress_dialog.setIndeterminate(false);
            progress_dialog.setCancelable(false);
            progress_dialog.show();
            OkHttpClient okHttpClient = public_func.get_okhttp_obj();
            Request request = new Request.Builder().url("https://" + host_save + "/control").method("OPTIONS", null).build();
            Call call = okHttpClient.newCall(request);
            progress_dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (keyEvent.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
                    call.cancel();
                }
                return false;
            });
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    progress_dialog.cancel();
                    if(!Objects.equals(e.getMessage(), "Canceled")){
                        Looper.prepare();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(login_activity.this);
                        alertDialog.setTitle(R.string.cannot_connect);
                        alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                        alertDialog.show();
                        Looper.loop();
                    }

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    progress_dialog.cancel();
                    if (response.code() != 204) {
                        Looper.prepare();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(login_activity.this);
                        alertDialog.setTitle(R.string.cannot_connect);
                        alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                        alertDialog.show();
                        Looper.loop();
                        return;
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (!host_list.has(host_save)) {
                        JsonObject object = new JsonObject();
                        object.addProperty("host", host_save);
                        object.addProperty("password_v2", password_save);
                        host_list.add(host_save, object);
                    }
                    editor.putString("host_list", new Gson().toJson(host_list));
                    editor.putString("host", host_save);
                    editor.putString("password_v2", password_save);
                    editor.apply();
                    start_edit();

                }
            });


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            ActivityCompat.requestPermissions(login_activity.this, new String[]{Manifest.permission.CAMERA}, 1);
            return true;
        });
        host_list = new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("host_list", "{}"))).getAsJsonObject();
        host_name_list = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : host_list.entrySet()) {
            host_name_list.add(entry.getKey());
        }
        Button history_host_button = findViewById(R.id.use_previous_button);
        history_host_button.setOnClickListener(history_host);
        Button save_button = findViewById(R.id.save_button);
        host = findViewById(R.id.host);
        password = findViewById(R.id.password);
        save_button.setOnClickListener(save_host);
    }

    private void start_edit() {
        Intent edit_post_activity = new Intent(context, post_list_activity.class);
        startActivity(edit_post_activity);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, scanner_activity.class);
                startActivity(intent);
                finish();
                return;
            }
            Snackbar.make(host, R.string.scan_qrcode, Snackbar.LENGTH_LONG).show();
        }
    }
}
