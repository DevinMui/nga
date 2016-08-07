package xyz.devinmui.zika;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class login extends AppCompatActivity {

    Api mApi = new Api();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginPress(View v) throws IOException {
        final EditText loginText = (EditText) findViewById(R.id.login_email);
        String str = "{\"email\": \"" + loginText.getText() + "\"}";
        mApi.post("/create_person", str, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Intent intent = new Intent(login.this, MainActivity.class);
                intent.putExtra("email", loginText.getText());
                login.this.startActivity(intent);
            }
        });
    }
}
