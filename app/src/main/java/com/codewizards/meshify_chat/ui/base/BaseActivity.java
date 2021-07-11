package com.codewizards.meshify_chat.ui.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.codewizards.meshify_chat.ui.splash.SplashActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSplashActivity();
    }

    private void showSplashActivity() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}