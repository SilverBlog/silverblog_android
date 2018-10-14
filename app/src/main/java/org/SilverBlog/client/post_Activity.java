package org.SilverBlog.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.SilverBlog.client.RecyclerViewAdapter.sharedPreferences;

public class post_Activity extends AppCompatActivity {
    EditText titleview;
    EditText nameview;
    EditText editTextview;
    int request_post_id;
    String action_name = "new";
    private Context context;
    private Boolean edit_menu;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (titleview.getText().length() == 0 || editTextview.getText().length() == 0) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                alertDialog.setTitle(R.string.content_not_none);
                alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                alertDialog.show();
                return false;
            }
            if (public_value.password == null) {
                String host_save;
                String password_save;
                sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                host_save = sharedPreferences.getString("host", null);
                password_save = sharedPreferences.getString("password", null);
                if (password_save == null || host_save == null) {
                    Intent main_activity = new Intent(post_Activity.this, main_Activity.class);
                    startActivity(main_activity);
                    finish();
                    return false;
                }
                host_save = host_save.replace("http://", "").replace("https://", "");
                public_value.host = host_save;
                public_value.password = password_save;
            }
            Gson gson = new Gson();
            content_json content = new content_json();
            if (action_name.equals("edit")) {
                content.post_id=request_post_id;
            }
            content.name=nameview.getText().toString();
            content.title=titleview.getText().toString();
            content.content=editTextview.getText().toString();
            content.sign=public_func.getMD5(titleview.getText().toString() + public_value.password);
            String json = gson.toJson(content);
            final ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
            if (action_name.equals("edit")) {
                action_name = "edit/post";
                if (edit_menu) {
                    action_name = "edit/menu";
                }
            }
            RequestBody body = RequestBody.create(public_value.JSON, json);
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + action_name).method("POST", body).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mpDialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                    alertDialog.setTitle(R.string.submit_error);
                    alertDialog.setNegativeButton(getString(R.string.ok_button), null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    mpDialog.cancel();
                    JsonParser parser = new JsonParser();
                    final JsonObject objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                    post_Activity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                            alertDialog.setTitle(R.string.submit_error);
                            String ok_button = getString(R.string.ok_button);
                            if (objects.get("status").getAsBoolean()) {
                                alertDialog.setTitle(R.string.submit_success);
                                ok_button = getString(R.string.visit_document);
                                alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (objects.get("status").getAsBoolean()) {
                                            Intent intent = new Intent();
                                            intent.setAction("org.silverblog.client");
                                            intent.putExtra("success", true);
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                            alertDialog.setNegativeButton(ok_button,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (objects.get("status").getAsBoolean()) {
                                                Uri uri = Uri.parse("https://" + public_value.host + "/post/" + objects.get("name").getAsString());
                                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                                Intent intent = new Intent();
                                                intent.setAction("org.silverblog.client");
                                                intent.putExtra("success", true);
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                finish();
                                            }
                                        }
                                    });
                            alertDialog.create().show();
                        }
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
        titleview = findViewById(R.id.titleview);
        editTextview = findViewById(R.id.mdcontent);
        nameview = findViewById(R.id.nameview);
        this.setTitle(getString(R.string.post_title));
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        Intent intent = getIntent();
        edit_menu = intent.getBooleanExtra("menu", false);

        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
        if (intent.getBooleanExtra("edit", false)) {
            action_name = "edit";
            this.setTitle(getString(R.string.edit_title));
            request_post_id = intent.getIntExtra("position", -1);
            final ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
            String active_name = "get_content/post";
            if (edit_menu) {
                active_name = "get_content/menu";
            }
            RequestBody body = RequestBody.create(public_value.JSON, "{\"post_id\":" + Integer.toString(request_post_id) + "}");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + active_name).method("POST", body).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    mpDialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                    alertDialog.setTitle(R.string.submit_error);
                    alertDialog.setNegativeButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    JsonParser parser = new JsonParser();
                    final JsonObject objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                    post_Activity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (editTextview.getText().length() == 0) {

                                titleview.setText(objects.get("title").getAsString());
                                editTextview.setText(objects.get("content").getAsString());
                            }
                            nameview.setText(objects.get("name").getAsString());

                        }
                    });
                    mpDialog.cancel();
                }

            });
        }
    }

    void handleSendText(Intent intent) {
        final String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        final String content = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (title == null) {
            final String[] content_split = content.split("\n");
            if (content_split[0].startsWith("# ")) {
                final String title_final = content_split[0].replace("# ", "");
                final String content_replace = content.replace(content_split[0] + "\n", "");
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                alertDialog.setTitle(R.string.notice);
                alertDialog.setMessage(R.string.notice_remove_title);
                alertDialog.setNeutralButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        titleview.setText(title_final);
                        editTextview.setText(content_replace);
                    }
                });
                alertDialog.setNegativeButton(R.string.cancel, null);
                alertDialog.show();
            }
        }
        titleview.setText(title);
        editTextview.setText(content);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
        alertDialog.setTitle(R.string.notice);
        alertDialog.setMessage(R.string.save_notice);
        alertDialog.setNeutralButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.show();

    }
}
