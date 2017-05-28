package reallct.qwe7002.smartblog_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button save = (Button) findViewById(R.id.button3);
        Button newpost = (Button) findViewById(R.id.button4);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        final EditText host = (EditText) findViewById(R.id.host);
        final EditText password = (EditText) findViewById(R.id.password);
        host.setText(sharedPreferences.getString("host", null));
        password.setText(sharedPreferences.getString("password", null));
        newpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (host.getText() != null) {
                    Intent newpost_activity = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newpost_activity);
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("host", String.valueOf(host.getText()));
                editor.putString("password", String.valueOf(password.getText()));
                editor.apply();
                finish();
            }
        });
    }
}
