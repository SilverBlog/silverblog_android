package com.reallct.qwe7002.smartblog_client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.zzhoujay.richtext.RichText;

public class post_preview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_preview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        RichText.fromMarkdown("# " + intent.getStringExtra(Intent.EXTRA_SUBJECT) + "\n" + intent.getStringExtra(Intent.EXTRA_TEXT)).into((TextView) findViewById(R.id.markdown_view));

    }

}
