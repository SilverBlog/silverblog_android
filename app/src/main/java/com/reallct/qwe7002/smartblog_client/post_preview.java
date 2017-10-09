package com.reallct.qwe7002.smartblog_client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.loader.DefaultLoader;
import com.yydcdut.rxmarkdown.syntax.text.TextFactory;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class post_preview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_preview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Context context = getApplicationContext();
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
        //RichText.fromMarkdown().into((TextView) findViewById(R.id.markdown_view));
        RxMDConfiguration rxMDConfiguration = new RxMDConfiguration.Builder(context)
                .setDefaultImageSize(100, 100)//default image width & height
                .setBlockQuotesColor(Color.LTGRAY)//default color of block quotes
                .setHeader1RelativeSize(1.6f)//default relative size of header1
                .setHeader2RelativeSize(1.5f)//default relative size of header2
                .setHeader3RelativeSize(1.4f)//default relative size of header3
                .setHeader4RelativeSize(1.3f)//default relative size of header4
                .setHeader5RelativeSize(1.2f)//default relative size of header5
                .setHeader6RelativeSize(1.1f)//default relative size of header6
                .setHorizontalRulesColor(Color.LTGRAY)//default color of horizontal rules's background
                .setInlineCodeBgColor(Color.LTGRAY)//default color of inline code's background
                .setCodeBgColor(Color.LTGRAY)//default color of code's background
                .setTodoColor(Color.DKGRAY)//default color
                .setTodoDoneColor(Color.DKGRAY)//default color of done
                .setUnOrderListColor(Color.BLACK)//default color of unorder list
                .setLinkColor(Color.BLUE)//default color of link text
                .setLinkUnderline(true)//default value of whether displays link underline
                .setRxMDImageLoader(new DefaultLoader(context))//default image loader
                .setDebug(false)//default value of debug
                .build();

        RxMarkdown.with(intent.getStringExtra(Intent.EXTRA_TEXT), this)
                .config(rxMDConfiguration)
                .factory(TextFactory.create())
                .intoObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        TextView markdown = (TextView) findViewById(R.id.markdown_view);
                        markdown.setMovementMethod(ScrollingMovementMethod.getInstance());
                        markdown.setText(charSequence, TextView.BufferType.SPANNABLE);
                    }
                });
    }

}
