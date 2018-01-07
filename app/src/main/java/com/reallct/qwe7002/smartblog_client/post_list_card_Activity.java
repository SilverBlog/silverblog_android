package com.reallct.qwe7002.smartblog_client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.reallct.qwe7002.smartblog_client.RecyclerViewAdapter.sharedPreferences;

public class post_list_card_Activity extends AppCompatActivity {
    private static final String MY_BROADCAST_TAG = "com.reallct.qwe7002.smartblog_client";
    SwipeRefreshLayout mSwipeRefreshWidget;
    NavigationView navigationView;
    private RecyclerView recyclerView;
    private MyReceiver receiver;
    private Context context;
    private Toolbar toolbar;

    public static Boolean isAbsURL(String URL) {
        try {
            URI u = new URI(URL);
            return u.isAbsolute();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getAbsUrl(String absolutePath, String relativePath) {
        try {
            URL absoluteUrl = new URL(absolutePath);
            URL parseUrl = new URL(absoluteUrl, relativePath);
            return parseUrl.toString();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    void handleSendText(Intent intent) {
        public_value.share_title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        public_value.share_text = intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    void start_login() {
        Intent main_activity = new Intent(post_list_card_Activity.this, main_Activity.class);
        startActivity(main_activity);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String host_save;
        String password_save;
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        host_save = sharedPreferences.getString("host", null);
        password_save = sharedPreferences.getString("password", null);
        if (password_save == null || host_save == null) {
            start_login();
        }
        public_value.host = host_save;
        public_value.password = password_save;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list_card);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Loading...");
        setSupportActionBar(toolbar);

        context = getApplicationContext();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                Snackbar.make(findViewById(R.id.fab), R.string.share_recive, Snackbar.LENGTH_LONG).show();
                handleSendText(intent);
            }
        }

        recyclerView = findViewById(R.id.my_recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent new_post_activity = new Intent(post_list_card_Activity.this, post_Activity.class);
                new_post_activity.putExtra("share_title", public_value.share_title);
                new_post_activity.putExtra("share_text", public_value.share_text);
                public_value.share_text = null;
                public_value.share_title = null;
                startActivity(new_post_activity);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MY_BROADCAST_TAG);

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        navigationView = findViewById(R.id.nav_view);
        mSwipeRefreshWidget = findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.colorPrimary);
        if (password_save != null && host_save != null) {
            mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new get_post_list_content().execute();
                    new get_menu_list_content().execute();
                }
            });
            new get_post_list_content().execute();
            new get_menu_list_content().execute();
            new get_system_info_content().execute();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_to_git_button:
                new push_to_git().execute();
                break;
            case R.id.logout:
                start_login();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.hasExtra("result")) {
                Snackbar.make(findViewById(R.id.fab), arg1.getStringExtra("result"), Snackbar.LENGTH_LONG).show();
            }
            if (arg1.getBooleanExtra("success", false)) {
                new get_post_list_content().execute();
                new get_menu_list_content().execute();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class push_to_git extends AsyncTask<Void, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(post_list_card_Activity.this);

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
        protected String doInBackground(Void... args) {
            return request.send_request("{}", "git_page_publish");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            String result_message = getString(R.string.git_push_error);
            if (objects.get("status").getAsBoolean()) {
                result_message = getString(R.string.submit_success);
            }
            Snackbar.make(findViewById(R.id.fab), result_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class get_post_list_content extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            mSwipeRefreshWidget.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... args) {
            String mode = "{}";
            String active_name = "get_list/post";
            return request.send_request(mode, active_name);
        }

        @Override
        protected void onPostExecute(String result) {
            mSwipeRefreshWidget.setRefreshing(false);
            JsonParser parser = new JsonParser();
            List<Post_List_Serialzable> post_list = new ArrayList<>();
            if (parser.parse(result).isJsonArray()) {
                final JsonArray result_array = parser.parse(result).getAsJsonArray();
                public_value.post_list = result_array;

                for (JsonElement item : result_array) {
                    JsonObject sub_item = item.getAsJsonObject();
                    post_list.add(new Post_List_Serialzable(sub_item.get("title").getAsString(), sub_item.get("excerpt").getAsString()));
                }
                if (result_array.size() == 0) {
                    Snackbar.make(mSwipeRefreshWidget, R.string.list_is_none, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.fab), R.string.network_error, Snackbar.LENGTH_LONG).show();
            }
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(post_list, post_list_card_Activity.this);
            recyclerView.setAdapter(adapter);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class get_system_info_content extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... args) {
            String mode = "{}";
            String active_name = "system_info";
            return request.send_request(mode, active_name);
        }

        @Override
        protected void onPostExecute(String result) {
            JsonParser parser = new JsonParser();
            JsonObject result_object = parser.parse(result).getAsJsonObject();
            if (!result_object.has("status")) {
                try {
                    if (result_object.get("api_version").getAsInt() < 2) {
                        new AlertDialog.Builder(post_list_card_Activity.this)
                                .setMessage(getString(R.string.api_too_low))
                                .show();
                    }
                    View headerView = navigationView.getHeaderView(0);
                    ImageView ivAvatar = headerView.findViewById(R.id.imageView);
                    String imageURL = result_object.get("author_image").getAsString();
                    if (!isAbsURL(imageURL)) {
                        imageURL = getAbsUrl(public_value.host, imageURL);
                    }

                    Glide.with(post_list_card_Activity.this).load(imageURL).error(R.mipmap.ic_launcher).transform(new CircleTransform(post_list_card_Activity.this)).into(ivAvatar);
                    TextView username = headerView.findViewById(R.id.username);
                    TextView desc = headerView.findViewById(R.id.desc);
                    username.setText(result_object.get("author_name").getAsString());
                    desc.setText(result_object.get("project_description").getAsString());
                    toolbar.setTitle(result_object.get("project_name").getAsString());
                } catch (Exception ex) {
                    return;
                }
            } else {
                Snackbar.make(findViewById(R.id.fab), R.string.network_error, Snackbar.LENGTH_LONG).show();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class get_menu_list_content extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... args) {
            String mode = "{}";
            String active_name = "get_list/menu";
            return request.send_request(mode, active_name);
        }

        @Override
        protected void onPostExecute(String result) {
            JsonParser parser = new JsonParser();
            if (parser.parse(result).isJsonArray()) {
                final JsonArray result_array = parser.parse(result).getAsJsonArray();
                public_value.menu_list = result_array;
                navigationView.getMenu().clear();
                int id = 0;
                for (JsonElement item : result_array) {
                    JsonObject sub_item = item.getAsJsonObject();
                    navigationView.getMenu().add(Menu.NONE, id, Menu.NONE, sub_item.get("title").getAsString());
                    id++;
                }
                navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();
                        JsonArray menu_list = public_value.menu_list;
                        JsonObject menu_item = menu_list.get(id).getAsJsonObject();
                        if (menu_item.has("absolute")) {
                            Uri uri = Uri.parse(menu_item.get("absolute").getAsString());
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            return false;
                        }
                        Intent intent = new Intent(context, post_Activity.class);
                        intent.putExtra("edit", true);
                        intent.putExtra("position", id);
                        intent.putExtra("menu", true);
                        intent.putExtra("share_title", public_value.share_title);
                        intent.putExtra("share_text", public_value.share_text);
                        public_value.share_text = null;
                        public_value.share_title = null;
                        startActivity(intent);
                        DrawerLayout drawer = findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        return false;
                    }
                });
            } else {
                Snackbar.make(findViewById(R.id.fab), R.string.network_error, Snackbar.LENGTH_LONG).show();
            }

        }
    }
}

