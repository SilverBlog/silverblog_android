package org.SilverBlog.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
            switch (menuItem.getItemId()) {
                case R.id.send_post_button:
                    if (public_value.password != null) {
                        Gson gson = new Gson();
                        content_json content = new content_json();
                        if (action_name.equals("edit")) {
                            content.setPost_id(request_post_id);
                        }
                        content.setName(nameview.getText().toString());
                        content.setTitle(titleview.getText().toString());
                        content.setContent(editTextview.getText().toString());
                        content.setsign(request.getMD5(titleview.getText().toString() + public_value.password));
                        String json = gson.toJson(content);
                        new push_post().execute(json);
                    }
                    break;
                default:
                    break;
            }
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

        context = getApplicationContext();
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
            new get_post_content().execute(Integer.toString(request_post_id));
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

    @SuppressLint("StaticFieldLeak")
    private class get_post_content extends AsyncTask<String, Integer, String> {

        ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);

        @Override
        protected void onPreExecute() {
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            String request_json = "{\"post_id\":" + args[0] + "}";
            String active_name = "get_content/post";
            if (edit_menu) {
                active_name = "get_content/menu";
            }
            return request.send_request(request_json, active_name);
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            if (objects.get("status").getAsBoolean()) {
                if (editTextview.getText().length() == 0) {

                    titleview.setText(objects.get("title").getAsString());
                    editTextview.setText(objects.get("content").getAsString());
                }
                nameview.setText(objects.get("name").getAsString());
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(post_Activity.this);
                alertDialog.setTitle(R.string.submit_error);
                alertDialog.setNegativeButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            }
        }
    }

    private class content_json {
        private int post_id;
        private String content;
        private String sign;
        private String title;
        private String name;

        void setName(String name) {
            this.name = name;
        }

        void setPost_id(int post_id) {
            this.post_id = post_id;
        }

        void setTitle(String title) {
            this.title = title;
        }

        void setContent(String Content) {
            this.content = Content;
        }

        void setsign(String sign) {
            this.sign = sign;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class push_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(post_Activity.this);

        @Override
        protected void onPreExecute() {
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(getString(R.string.loading));
            mpDialog.setMessage(getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            if (action_name.equals("edit")) {
                action_name = "edit/post";
                if (edit_menu) {
                    action_name = "edit/menu";
                }
            }
            return request.send_request(args[0], action_name);
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
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
    }
}
