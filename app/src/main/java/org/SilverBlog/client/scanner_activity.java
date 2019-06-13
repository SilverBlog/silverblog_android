package org.SilverBlog.client;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class scanner_activity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("scanner", rawResult.getText()); // Prints scan results
        Log.v("scanner", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        JsonParser parser = new JsonParser();
        JsonObject objects;
        try {
            objects = parser.parse(rawResult.getText()).getAsJsonObject();
        } catch (IllegalStateException e) {
            Snackbar.make(mScannerView, R.string.QRcode_error, Snackbar.LENGTH_LONG)
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
        public_value.host = host_save;
        public_value.password = password_save;
        Intent edit_post_activity = new Intent(getApplicationContext(), post_list_activity.class);
        startActivity(edit_post_activity);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), main_activity.class);
        startActivity(intent);
        finish();

    }
}
