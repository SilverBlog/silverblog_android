package org.SilverBlog.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
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

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.SilverBlog.client.recycler_view_adapter.sharedpreferences;

public class edit_activity extends AppCompatActivity {
    private EditText title_view;
    private EditText name_view;
    private EditText content_view;
    String post_uuid;
     String action_name = "new";
    Boolean edit_mode = false;
    private Context context;
    private Boolean edit_menu;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (title_view.getText().length() == 0 || content_view.getText().length() == 0) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
                alertDialog.setTitle(R.string.content_not_none);
                alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                alertDialog.show();
                return false;
            }
            if (!public_value.init) {
                String host_save;
                String password_save;
                sharedpreferences = getSharedPreferences("data", MODE_PRIVATE);
                host_save = sharedpreferences.getString("host", null);
                password_save = sharedpreferences.getString("password_v2", null);
                if (password_save == null || host_save == null) {
                    Intent main_activity = new Intent(edit_activity.this, org.SilverBlog.client.main_activity.class);
                    startActivity(main_activity);
                    finish();
                    return false;
                }
                public_value.init = true;
                host_save = host_save.replace("http://", "").replace("https://", "");
                public_value.host = host_save;
                public_value.password = password_save;
            }
            Gson gson = new Gson();
            content_json json_obj = new content_json();
            json_obj.post_uuid = "";
            if (edit_mode) {
                json_obj.post_uuid = post_uuid;
                action_name = "edit/post";
                if (edit_menu) {
                    action_name = "edit/menu";
                }
            }
            json_obj.name = name_view.getText().toString();
            json_obj.title = title_view.getText().toString();
            json_obj.content = content_view.getText().toString();
            json_obj.send_time = System.currentTimeMillis();
            String sign_message = json_obj.title + json_obj.name + public_func.get_hash(json_obj.content, "SHA-512");
            if (edit_mode) {
                sign_message += json_obj.post_uuid;
            }
            json_obj.sign = public_func.get_hmac_hash(sign_message, public_value.password + json_obj.send_time, "HmacSHA512");
            String json = gson.toJson(json_obj);
            final ProgressDialog mpDialog = new ProgressDialog(edit_activity.this);
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
            RequestBody body = RequestBody.create(public_value.JSON, json);
            OkHttpClient okHttpClient = public_func.get_okhttp_obj();
            Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + public_value.API_VERSION + "/" + action_name).method("POST", body).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mpDialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
                    alertDialog.setTitle(R.string.network_error);
                    alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    mpDialog.cancel();
                    if (response.code() != 200) {
                        Looper.prepare();
                        Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                        Looper.loop();
                        return;
                    }
                    JsonParser parser = new JsonParser();
                    JsonObject objects = null;
                    try {
                        objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final JsonObject finalObjects = objects;
                    edit_activity.this.runOnUiThread(() -> {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
                        alertDialog.setTitle(R.string.submit_error);
                        String ok_button = getString(R.string.ok_button);
                        assert finalObjects != null;
                        if (finalObjects.get("status").getAsBoolean()) {
                            alertDialog.setTitle(R.string.submit_success);
                            ok_button = getString(R.string.visit_document);
                            alertDialog.setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
                                if (finalObjects.get("status").getAsBoolean()) {
                                    Intent intent = new Intent();
                                    intent.setAction(context.getPackageName());
                                    intent.putExtra("success", true);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    finish();
                                }
                            });
                        }
                        alertDialog.setNegativeButton(ok_button,
                                (dialogInterface, i) -> {
                                    if (finalObjects.get("status").getAsBoolean()) {
                                        Uri uri = Uri.parse("https://" + public_value.host + "/post/" + finalObjects.get("name").getAsString());
                                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                        Intent intent = new Intent();
                                        intent.setAction(context.getPackageName());
                                        intent.putExtra("success", true);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                        finish();
                                    }
                                });
                        alertDialog.create().show();
                    });
                }
            });
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = getApplicationContext();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title_view = findViewById(R.id.titleview);
        content_view = findViewById(R.id.mdcontent);
        name_view = findViewById(R.id.nameview);
        this.setTitle(getString(R.string.post_title));
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        Intent intent = getIntent();
        edit_menu = intent.getBooleanExtra("menu", false);

        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                final String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                final String content = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (title == null) {
                    final String[] content_split = content.split("\n");
                    if (content_split[0].startsWith("# ")) {
                        final String title_final = content_split[0].replace("# ", "");
                        final String content_replace = content.replace(content_split[0] + "\n", "");
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
                        alertDialog.setTitle(R.string.notice);
                        alertDialog.setMessage(R.string.notice_remove_title);
                        alertDialog.setNeutralButton(R.string.ok_button, (dialogInterface, i) -> {
                            title_view.setText(title_final);
                            content_view.setText(content_replace);
                        });
                        alertDialog.setNegativeButton(R.string.cancel, null);
                        alertDialog.show();
                    }
                }
                title_view.setText(title);
                content_view.setText(content);
            }
        }
        edit_mode = intent.getBooleanExtra("edit", false);
        if (edit_mode) {
            this.setTitle(getString(R.string.edit_title));
            post_uuid = intent.getStringExtra("uuid");
            final ProgressDialog mpDialog = new ProgressDialog(edit_activity.this);
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
            String active_name = "get/content/post";
            if (edit_menu) {
                active_name = "get/content/menu";
            }
            request_json request_obj = new request_json();
            request_obj.post_uuid = post_uuid;
            RequestBody body = RequestBody.create(public_value.JSON, new Gson().toJson(request_obj));
            OkHttpClient okHttpClient = public_func.get_okhttp_obj();
            Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + public_value.API_VERSION + "/" + active_name).method("POST", body).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    mpDialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
                    alertDialog.setTitle(R.string.submit_error);
                    alertDialog.setNegativeButton(getString(R.string.ok_button), (dialogInterface, i) -> finish());
                }

                @Override
                public void onResponse(Call call, final Response response) {
                    if (response.code() != 200) {
                        Looper.prepare();
                        Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                        Looper.loop();
                        return;
                    }
                    runOnUiThread(() -> {
                        JsonParser parser = new JsonParser();
                        JsonObject objects = null;
                        try {
                            assert response.body() != null;
                            objects = parser.parse(Objects.requireNonNull(response.body().string())).getAsJsonObject();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert objects != null;
                        if (title_view.getText().length() == 0) {
                            title_view.setText(objects.get("title").getAsString());
                        }
                        if (content_view.getText().length() == 0) {
                            content_view.setText(objects.get("content").getAsString());
                        }
                        name_view.setText(objects.get("name").getAsString());
                        mpDialog.cancel();
                    });
                }

            });
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(edit_activity.this);
        alertDialog.setTitle(R.string.notice);
        alertDialog.setMessage(R.string.save_notice);
        alertDialog.setNeutralButton(R.string.ok_button, (dialogInterface, i) -> finish());
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.show();

    }
}

class request_json {
    String post_uuid;
}

class content_json {
    String post_uuid;
    String content;
    String sign;
    String title;
    String name;
    long send_time;
}
