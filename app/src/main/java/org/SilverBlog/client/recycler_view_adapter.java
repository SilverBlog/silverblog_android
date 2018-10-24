package org.SilverBlog.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class recycler_view_adapter extends RecyclerView.Adapter<recycler_view_adapter.card_view_holder> {

    static SharedPreferences sharedPreferences;
    private List<post_list_serialzable> postlistserialzables;
    private Context context;

    recycler_view_adapter(List<post_list_serialzable> postlistserialzables, Context context) {
        this.postlistserialzables = postlistserialzables;
        this.context = context;
    }

    @NonNull
    @Override
    public card_view_holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview, viewGroup, false);
        return new card_view_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull card_view_holder personViewHolder, @SuppressLint("RecyclerView") final int position) {

        personViewHolder.title.setText(postlistserialzables.get(position).title);
        personViewHolder.excerpt.setText(postlistserialzables.get(position).excerpt);
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
                                                final ProgressDialog mpDialog = new ProgressDialog(context);
                                                mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                                mpDialog.setTitle(context.getString(R.string.loading));
                                                mpDialog.setMessage(context.getString(R.string.loading_message));
                                                mpDialog.setIndeterminate(false);
                                                mpDialog.setCancelable(false);
                                                mpDialog.show();
                                                JsonObject postobj = public_value.post_list.get(position).getAsJsonObject();
                                                String title = postobj.get("title").getAsString();
                                                RequestBody body = RequestBody.create(public_value.JSON, "{\"post_id\":" + Integer.toString(position) + ",\"sign\":\"" + public_func.getMD5(Integer.toString(position) + title + public_value.password) + "\"}");
                                                Request request = new Request.Builder().url("https://" + public_value.host + "/control/delete").method("POST", body).build();
                                                OkHttpClient okHttpClient = new OkHttpClient();
                                                Call call = okHttpClient.newCall(request);
                                                call.enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        mpDialog.cancel();
                                                        Intent intent = new Intent();
                                                        intent.putExtra("result", context.getString(R.string.submit_error));
                                                        intent.putExtra("success", false);
                                                        intent.setAction(public_value.MY_BROADCAST_TAG);
                                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        mpDialog.cancel();
                                                        JsonParser parser = new JsonParser();
                                                        final JsonObject objects = parser.parse(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
                                                        String result_message = context.getString(R.string.submit_error);
                                                        if (objects.get("status").getAsBoolean()) {
                                                            result_message = context.getString(R.string.submit_success);
                                                        }
                                                        Intent intent = new Intent();
                                                        intent.putExtra("result", result_message);
                                                        intent.putExtra("success", true);
                                                        intent.setAction(public_value.MY_BROADCAST_TAG);
                                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                    }
                                                });
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
        return postlistserialzables.size();
    }

    static class card_view_holder extends RecyclerView.ViewHolder {

        CardView cardView = itemView.findViewById(R.id.card_view);
        TextView title = itemView.findViewById(R.id.title);
        TextView excerpt = itemView.findViewById(R.id.excerpt);

        card_view_holder(final View itemView) {
            super(itemView);
        }


    }
}