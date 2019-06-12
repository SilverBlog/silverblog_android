package org.SilverBlog.client;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.activity.CaptureActivity;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;


public class main_activity extends AppCompatActivity {
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

    View.OnClickListener history_host = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(view.getContext()).setTitle(R.string.select_config).setItems(host_name_list.toArray(new String[0]), (dialogInterface, index) -> {
                JsonObject host_info = host_list.get(host_name_list.get(index)).getAsJsonObject();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                host_save = host_info.get("host").getAsString();
                if (host_info.has("password")) {
                    String password_v2 = public_func.get_hmac_hash(host_info.get("password").getAsString(), "SiLvErBlOg", "HmacSHA256");
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
    View.OnClickListener save_host = new View.OnClickListener() {
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
            password_save = public_func.get_hmac_hash(Objects.requireNonNull(public_func.get_hash(String.valueOf(password.getText()), "MD5")), "SiLvErBlOg", "HmacSHA256");
            final ProgressDialog mpDialog = new ProgressDialog(main_activity.this);
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
            OkHttpClient okHttpClient = public_func.get_okhttp_obj();
            Request request = new Request.Builder().url("https://" + host_save + "/control").method("OPTIONS", null).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mpDialog.cancel();
                    Snackbar.make(view, R.string.cannot_connect, Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    mpDialog.cancel();
                    if (response.code() != 204) {
                        Snackbar.make(view, R.string.cannot_connect, Snackbar.LENGTH_LONG).show();
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
                    Log.d("silverblog", "onActivityResult: " + password_save);
                    editor.apply();
                    start_edit();

                }
            });


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (ContextCompat.checkSelfPermission(main_activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(main_activity.this, new String[]{Manifest.permission.CAMERA}, 1);
                return false;
            }
            Intent intent = new Intent(main_activity.this, CaptureActivity.class);
            startActivityForResult(intent, 0);
            return true;
        });
        host_list = new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("host_list", "{}"))).getAsJsonObject();
        host_name_list = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : host_list.entrySet()) {
            host_name_list.add(entry.getKey());
        }
        Button history_host_button = findViewById(R.id.use_old);
        history_host_button.setOnClickListener(history_host);
        Button save_button = findViewById(R.id.save_button);
        host = findViewById(R.id.host);
        password = findViewById(R.id.password);
        save_button.setOnClickListener(save_host);
    }

    void start_edit() {
        public_value.host = host_save;
        public_value.password = password_save;
        Intent edit_post_activity = new Intent(getApplicationContext(), post_list_activity.class);
        startActivity(edit_post_activity);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(main_activity.this, CaptureActivity.class);
                startActivityForResult(intent, 0);
                return;
            }
            Snackbar.make(host, R.string.scan_qr, Snackbar.LENGTH_LONG).show();
        }
    }


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
            host_save = objects.get("H").getAsString();
            password_save = objects.get("P").getAsString();
            if (password_save.length() == 0 || host_save.length() == 0) {
                Snackbar.make(host, R.string.check_config, Snackbar.LENGTH_LONG).show();
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
    }
}
