package reallct.qwe7002.smartblog_client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;


public class main_Activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button save_button = (Button) findViewById(R.id.save_button);
        Button new_post_button = (Button) findViewById(R.id.send_button);
        Button edit_post_button = (Button) findViewById(R.id.edit_button);
        final EditText host = (EditText) findViewById(R.id.host);
        final EditText password = (EditText) findViewById(R.id.password);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        final String host_save = sharedPreferences.getString("host", null);
        final String password_save = sharedPreferences.getString("password", null);
        host.setText(host_save);
        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        new_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host_save != null && password_save != null) {
                    Intent new_post_activity = new Intent(main_Activity.this, new_post_Activity.class);
                    startActivity(new_post_activity);
                } else {
                    if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        edit_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host_save != null && password_save != null) {
                    Intent edit_post_activity = new Intent(main_Activity.this, post_list_Activity.class);
                    startActivity(edit_post_activity);
                } else {
                    if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (host_save != null && password_save != null) {
                    String hosturl = String.valueOf(host.getText());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("host", String.valueOf(hosturl));
                    editor.putString("password", silverblog_connect.getMD5(String.valueOf(password.getText())));
                    editor.apply();
                    Snackbar.make(view, "配置已保存", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "请先配置服务器信息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
