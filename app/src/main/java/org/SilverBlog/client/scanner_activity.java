package org.SilverBlog.client;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class scanner_activity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.scanner_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.scan_title);
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v("scanner", rawResult.getText());
        Log.v("scanner", rawResult.getBarcodeFormat().toString());
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        JsonParser parser = new JsonParser();
        JsonObject objects;
        try {
            objects = parser.parse(rawResult.getText()).getAsJsonObject();
        } catch (IllegalStateException | JsonSyntaxException e) {
            Snackbar.make(mScannerView, R.string.qrcode_format_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            mScannerView.resumeCameraPreview(this);
            return;
        }
        String host_save = objects.get("H").getAsString();
        String password_save = objects.get("P").getAsString();
        if (password_save.length() == 0 || host_save.length() == 0) {
            Snackbar.make(mScannerView, R.string.check_config, Snackbar.LENGTH_LONG).show();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JsonObject host_list = new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("host_list", "{}"))).getAsJsonObject();
        ArrayList<String> host_name_list = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : host_list.entrySet()) {
            host_name_list.add(entry.getKey());
        }
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
        Intent edit_post_activity = new Intent(getApplicationContext(), post_list_activity.class);
        startActivity(edit_post_activity);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), login_activity.class);
        startActivity(intent);
        finish();
    }
}
