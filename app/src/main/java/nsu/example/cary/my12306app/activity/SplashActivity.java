package nsu.example.cary.my12306app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //打开 MainActivity
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));


        // 自动登录
        SharedPreferences pref = getSharedPreferences("user", 0);
        String username = pref.getString("username", "");
        String password = pref.getString("password", "");

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }

        // close splash screen
        finish();
    }
}
