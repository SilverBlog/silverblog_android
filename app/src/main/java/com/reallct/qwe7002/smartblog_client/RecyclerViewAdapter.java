package com.reallct.qwe7002.smartblog_client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;


/**
 * Created by qwe7002 on 2017/10/8.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.NewsViewHolder> {

    private List<Post_List_Serialzable> post_list_serialzables;
    private Context context;
    static SharedPreferences sharedPreferences;
    private static final String MY_BROADCAST_TAG = "com.reallct.qwe7002.smartblog_client";

    public RecyclerViewAdapter(List<Post_List_Serialzable> post_list_serialzables, Context context) {
        this.post_list_serialzables = post_list_serialzables;
        this.context = context;

    }


    //自定义ViewHolder类
    static class NewsViewHolder extends RecyclerView.ViewHolder {

        CardView cardView = (CardView) itemView.findViewById(R.id.card_view);
        TextView title = (TextView) itemView.findViewById(R.id.title);
        TextView excerpt = (TextView) itemView.findViewById(R.id.excerpt);

        public NewsViewHolder(final View itemView) {
            super(itemView);
            //设置TextView背景为半透明
            //news_title.setBackgroundColor(Color.argb(20, 0, 0, 0));
        }


    }

    @Override
    public RecyclerViewAdapter.NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview, viewGroup, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.NewsViewHolder personViewHolder, final int position) {

        personViewHolder.title.setText(post_list_serialzables.get(position).getTitle());
        personViewHolder.excerpt.setText(post_list_serialzables.get(position).getExcerpt());
        personViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle(R.string.select).setItems(new String[]{context.getString(R.string.modify), context.getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(context, post_Activity.class);
                                intent.putExtra("edit", true);
                                intent.putExtra("position", position);
                                intent.putExtra("share_title", public_value.share_title);
                                intent.putExtra("share_text", public_value.share_text);
                                public_value.share_text = null;
                                public_value.share_title = null;
                                context.startActivity(intent);
                                break;
                            case 1:
                                new AlertDialog.Builder(context).setTitle(R.string.notice).setMessage(R.string.delete_notify).setNeutralButton(R.string.ok_button,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                JsonObject postobj = public_value.post_list.get(position).getAsJsonObject();
                                                String title = postobj.get("title").getAsString();
                                                new delete_post().execute(Integer.toString(position), title);
                                            }
                                        }).setNegativeButton(R.string.cancel, null).show();
                                break;
                        }
                    }
                }).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return post_list_serialzables.size();
    }

    private class delete_post extends AsyncTask<String, Integer, String> {
        ProgressDialog mpDialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mpDialog.setTitle(context.getString(R.string.loading));
            mpDialog.setMessage(context.getString(R.string.loading_message));
            mpDialog.setIndeterminate(false);
            mpDialog.setCancelable(false);
            mpDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            return request.send_request("{\"post_id\":" + args[0] + ",\"encode\":\"" + request.getMD5(args[0] + args[1] + public_value.password) + "\"}", "delete");
        }

        @Override
        protected void onPostExecute(String result) {
            mpDialog.cancel();
            JsonParser parser = new JsonParser();
            final JsonObject objects = parser.parse(result).getAsJsonObject();
            String result_message = context.getString(R.string.submit_error);
            if (objects.get("status").getAsBoolean()) {
                result_message = context.getString(R.string.submit_success);
            }
            Intent intent = new Intent();
            intent.putExtra("result", result_message);
            intent.putExtra("success", true);
            intent.setAction(MY_BROADCAST_TAG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}