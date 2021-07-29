package com.codewizards.meshify_chat.ui.signup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.ui.home.MainActivity;
import com.codewizards.meshify_chat.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText mUserName;

    @BindView(R.id.register_button)
    Button mRegisterButton;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mUserName.setText(Build.MANUFACTURER + " " + Build.MODEL);
        this.mUserName.setEnabled(true);
        this.mRegisterButton.setEnabled(true);
    }

    @OnClick({R.id.register_button})
    public void attemptRegister(View v){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREFS_USERNAME, mUserName.getText().toString());
        editor.apply();

        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();

    }
}