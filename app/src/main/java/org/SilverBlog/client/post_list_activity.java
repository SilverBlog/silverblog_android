package org.SilverBlog.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.SilverBlog.client.recycler_view_adapter.sharedpreferences;

public class post_list_activity extends AppCompatActivity {
    private SwipeRefreshLayout swipe_refresh_widget;
    private NavigationView navigation_view;
    private boolean unload_system_info = true;
    private RecyclerView recycler_view;
    private Context context;
    private Toolbar toolbar;

    private static Boolean is_abs_url(String URL) throws URISyntaxException {
            URI u = new URI(URL);
            return u.isAbsolute();
    }

    private static String get_abs_url(String absolutePath, String relativePath) throws MalformedURLException {
        URL absoluteUrl = new URL(absolutePath);
        URL parseUrl = new URL(absoluteUrl, relativePath);
        return parseUrl.toString();
    }

    private void start_login() {
        Intent main_activity = new Intent(context, login_activity.class);
        startActivity(main_activity);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        String host_save;
        String password_save;
        sharedpreferences = getSharedPreferences("data", MODE_PRIVATE);
        host_save = sharedpreferences.getString("host", null);
        password_save = sharedpreferences.getString("password_v2", null);
        public_value.init = true;
        if (password_save == null || host_save == null) {
            start_login();
            return;
        }
        host_save = host_save.replace("http://", "").replace("https://", "");
        public_value.host = host_save;
        public_value.password = password_save;
        setContentView(R.layout.activity_post_list_card);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Loading...");
        setSupportActionBar(toolbar);
        recycler_view = findViewById(R.id.my_recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setHasFixedSize(true);
        recycler_view.setItemAnimator(new DefaultItemAnimator());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        result_receiver receiver = new result_receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(context.getPackageName());
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent new_post_activity = new Intent(context, edit_activity.class);
            startActivity(new_post_activity);
        });


        navigation_view = findViewById(R.id.nav_view);
        swipe_refresh_widget = findViewById(R.id.swipe_refresh_widget);
        swipe_refresh_widget.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh_widget.setOnRefreshListener(() -> {
            if (unload_system_info) {
                get_system_info();
            }
            get_post_list();
            get_menu_list();
        });

        get_post_list();
        get_menu_list();
        get_system_info();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_git_button:
                final ProgressDialog dialog = new ProgressDialog(post_list_activity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(getString(R.string.loading));
                dialog.setMessage(getString(R.string.loading_message));
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                dialog.show();
                OkHttpClient okHttpClient = public_func.get_okhttp_obj();
                Gson gson = new Gson();
                sign_json request_json_obj = new sign_json();
                request_json_obj.send_time = System.currentTimeMillis();
                request_json_obj.sign = public_func.get_hmac_hash("git_page_publish", public_value.password + request_json_obj.send_time, "HmacSHA512");
                RequestBody body = RequestBody.create(gson.toJson(request_json_obj), final_value.JSON);
                Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + final_value.API_VERSION + "/git_page_publish").method("POST", body).build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        dialog.cancel();
                        Snackbar.make(findViewById(R.id.toolbar), R.string.git_push_error, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        dialog.cancel();
                        if (response.code() != 200) {
                            Looper.prepare();
                            Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                            Looper.loop();
                            return;
                        }
                        JsonParser parser = new JsonParser();
                        final JsonObject objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                        int result_message = R.string.git_push_error;
                        if (objects.get("status").getAsBoolean()) {
                            result_message = R.string.submit_success;
                        }
                        Snackbar.make(findViewById(R.id.toolbar), result_message, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                break;
            case R.id.logout:
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.remove("host");
                editor.remove("password_v2");
                editor.apply();
                start_login();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void get_post_list() {
        swipe_refresh_widget.setRefreshing(true);
        Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + final_value.API_VERSION + "/get/list/post").build();
        OkHttpClient okHttpClient = public_func.get_okhttp_obj();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                swipe_refresh_widget.setRefreshing(false);
                Snackbar.make(findViewById(R.id.toolbar), R.string.network_error, Snackbar.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() != 200) {
                    Looper.prepare();
                    swipe_refresh_widget.setRefreshing(false);
                    Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                    Looper.loop();
                    return;
                }
                runOnUiThread(() -> {
                    swipe_refresh_widget.setRefreshing(false);
                    JsonParser parser = new JsonParser();
                    final List<post_list> post_list = new ArrayList<>();

                    JsonArray result_array = null;
                    try {
                        result_array = parser.parse(Objects.requireNonNull(Objects.requireNonNull(response.body()).string())).getAsJsonArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    public_value.post_list = result_array;

                    assert result_array != null;
                    for (JsonElement item : result_array) {
                        JsonObject sub_item = item.getAsJsonObject();
                        post_list list_obj = new post_list();
                        list_obj.title = sub_item.get("title").getAsString();
                        list_obj.excerpt = sub_item.get("excerpt").getAsString();
                        list_obj.uuid = sub_item.get("uuid").getAsString();
                        post_list.add(list_obj);
                    }
                    if (result_array.size() == 0) {
                        Snackbar.make(swipe_refresh_widget, R.string.list_is_none, Snackbar.LENGTH_LONG).show();
                    }
                    recycler_view_adapter adapter = new recycler_view_adapter(post_list, post_list_activity.this);
                    recycler_view.setAdapter(adapter);
                });
            }
        });

    }

    private void get_system_info() {
        Request request = new Request.Builder().url("https://" + public_value.host + "/control/system_info").build();
        OkHttpClient okHttpClient = public_func.get_okhttp_obj();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Snackbar.make(findViewById(R.id.toolbar), R.string.network_error, Snackbar.LENGTH_LONG).show();
                Looper.loop();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() != 200) {
                    Looper.prepare();
                    Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                    Looper.loop();
                    return;
                }
                unload_system_info = false;
                runOnUiThread(() -> {
                    JsonParser parser = new JsonParser();
                    JsonObject result_object = null;

                    try {

                        result_object = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert result_object != null;
                    if (result_object.get("api_version").getAsInt() < final_value.current_api_code) {
                        new AlertDialog.Builder(post_list_activity.this)
                                .setMessage(getString(R.string.api_too_low))
                                .show();
                        return;
                    }
                    if (result_object.get("api_version").getAsInt() > final_value.current_api_code) {
                        new AlertDialog.Builder(post_list_activity.this)
                                .setMessage(R.string.api_too_high)
                                .show();
                        return;
                    }
                    View header_view = navigation_view.getHeaderView(0);
                    ImageView image_view = header_view.findViewById(R.id.imageView);
                    String image_url = result_object.get("author_image").getAsString();
                    boolean is_abs = false;
                    try {
                        is_abs = is_abs_url(image_url);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        image_url = null;
                    }
                    if (!is_abs && image_url != null) {
                        try {
                            image_url = get_abs_url(public_value.host, image_url);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            image_url = null;
                        }
                    }
                    if (image_url != null) {
                        Glide.with(post_list_activity.this).load(image_url).apply(RequestOptions.circleCropTransform()).into(image_view);
                    }
                    TextView username = header_view.findViewById(R.id.username);
                    TextView desc = header_view.findViewById(R.id.desc);
                    username.setText(result_object.get("author_name").getAsString());
                    desc.setText(result_object.get("project_description").getAsString());
                    toolbar.setTitle(result_object.get("project_name").getAsString());
                });
            }
        });

    }

    private void get_menu_list() {
        Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + final_value.API_VERSION + "/get/list/menu").build();
        OkHttpClient okHttpClient = public_func.get_okhttp_obj();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Snackbar.make(findViewById(R.id.toolbar), R.string.network_error, Snackbar.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() != 200) {
                    Looper.prepare();
                    Snackbar.make(findViewById(R.id.toolbar), getString(R.string.request_error) + response.code(), Snackbar.LENGTH_LONG).show();
                    Looper.loop();
                    return;
                }
                runOnUiThread(() -> {
                    JsonParser parser = new JsonParser();
                    JsonArray result_array = null;

                    try {
                        result_array = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    public_value.menu_list = result_array;
                    navigation_view.getMenu().clear();
                    int index = 0;
                    assert result_array != null;
                    for (JsonElement item : result_array) {
                        JsonObject sub_item = item.getAsJsonObject();
                        navigation_view.getMenu().add(Menu.NONE, index, Menu.NONE, sub_item.get("title").getAsString());
                        index++;
                    }
                    navigation_view.setNavigationItemSelectedListener(item -> {
                        int id1 = item.getItemId();
                        JsonArray menu_list = public_value.menu_list;
                        JsonObject menu_item = menu_list.get(id1).getAsJsonObject();
                        if (menu_item.has("absolute")) {
                            Uri uri = Uri.parse(menu_item.get("absolute").getAsString());
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            return false;
                        }
                        Intent intent = new Intent(context, edit_activity.class);
                        intent.putExtra("edit", true);
                        intent.putExtra("uuid", menu_item.get("uuid").getAsString());
                        intent.putExtra("menu", true);
                        startActivity(intent);
                        DrawerLayout drawer = findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        return false;
                    });
                });


            }
        });
    }

    class result_receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("result")) {
                Snackbar.make(findViewById(R.id.toolbar), Objects.requireNonNull(intent.getStringExtra("result")), Snackbar.LENGTH_LONG).show();
            }
            if (intent.getBooleanExtra("success", false)) {
                get_post_list();
                get_menu_list();
            }
        }
    }
}



class recycler_view_adapter extends RecyclerView.Adapter<recycler_view_adapter.card_view_holder> {

    static SharedPreferences sharedpreferences;
    private final List<post_list> post_list;
    private final Context context;

    recycler_view_adapter(List<post_list> post_list, Context context) {
        this.post_list = post_list;
        this.context = context;
    }

    @NonNull
    @Override
    public card_view_holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview, viewGroup, false);
        return new card_view_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull card_view_holder card_view_holder_obj, @SuppressLint("RecyclerView") final int position) {

        card_view_holder_obj.title.setText(post_list.get(position).title);
        card_view_holder_obj.excerpt.setText(post_list.get(position).excerpt);
        card_view_holder_obj.card_view.setOnClickListener(v -> new AlertDialog.Builder(context).setTitle(R.string.select).setItems(new String[]{context.getString(R.string.modify), context.getString(R.string.delete)}, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    //Modify
                    Intent intent = new Intent(context, edit_activity.class);
                    intent.putExtra("edit", true);
                    intent.putExtra("uuid", post_list.get(position).uuid);
                    context.startActivity(intent);
                    break;
                case 1:
                    //Delete
                    new AlertDialog.Builder(context).setTitle(R.string.notice).setMessage(R.string.delete_notify).setNeutralButton(R.string.ok_button,
                            (dialogInterface1, i1) -> {
                                final ProgressDialog dialog = new ProgressDialog(context);
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setTitle(context.getString(R.string.loading));
                                dialog.setMessage(context.getString(R.string.loading_message));
                                dialog.setIndeterminate(false);
                                dialog.setCancelable(false);
                                dialog.show();
                                JsonObject post_obj = public_value.post_list.get(position).getAsJsonObject();
                                sign_json request_json_obj = new sign_json();
                                Gson gson = new Gson();
                                request_json_obj.post_uuid = post_list.get(position).uuid;
                                request_json_obj.send_time = System.currentTimeMillis();
                                request_json_obj.sign = public_func.get_hmac_hash(request_json_obj.post_uuid + post_obj.get("title").getAsString() + post_obj.get("name").getAsString(), public_value.password + request_json_obj.send_time, "HmacSHA512");
                                RequestBody body = RequestBody.create(gson.toJson(request_json_obj), final_value.JSON);
                                Request request = new Request.Builder().url("https://" + public_value.host + "/control/" + final_value.API_VERSION + "/delete").method("POST", body).build();
                                OkHttpClient okHttpClient = public_func.get_okhttp_obj();
                                Call call = okHttpClient.newCall(request);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        dialog.cancel();
                                        Intent broadcast_intent = new Intent();
                                        broadcast_intent.putExtra("result", context.getString(R.string.submit_error));
                                        broadcast_intent.putExtra("success", false);
                                        broadcast_intent.setAction(context.getPackageName());
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast_intent);
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        dialog.cancel();
                                        Intent broadcast_intent = new Intent();
                                        broadcast_intent.setAction(context.getPackageName());
                                        if (response.code() != 200) {
                                            broadcast_intent.putExtra("result", context.getString(R.string.request_error) + response.code());
                                            broadcast_intent.putExtra("success", false);
                                        }
                                        if (response.code() == 200) {
                                            JsonParser parser = new JsonParser();
                                            final JsonObject objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                                            String result_message = context.getString(R.string.submit_error);
                                            if (objects.get("status").getAsBoolean()) {
                                                result_message = context.getString(R.string.submit_success);
                                            }
                                            broadcast_intent.putExtra("result", result_message);
                                            broadcast_intent.putExtra("success", true);
                                        }
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast_intent);
                                    }
                                });
                            }).setNegativeButton(R.string.cancel, null).show();
                    break;
            }
        }).show());

    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    static class card_view_holder extends RecyclerView.ViewHolder {
        final CardView card_view = itemView.findViewById(R.id.card_view);
        final TextView title = itemView.findViewById(R.id.title);
        final TextView excerpt = itemView.findViewById(R.id.excerpt);

        card_view_holder(final View item) {
            super(item);
        }
    }
}

class sign_json {
    String post_uuid;
    String sign;
    long send_time;
}

class post_list {
    String title;
    String excerpt;
    String uuid;
}

