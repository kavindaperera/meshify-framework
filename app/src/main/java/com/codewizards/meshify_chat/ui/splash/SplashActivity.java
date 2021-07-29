package com.codewizards.meshify_chat.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.ui.home.MainActivity;
import com.codewizards.meshify_chat.ui.signup.SignupActivity;
import com.codewizards.meshify_chat.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(Constants.PREFS_USERNAME, null);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if(username != null) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                }
                finish();
            }
        }, 1000);

    }
}