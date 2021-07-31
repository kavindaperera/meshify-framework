package com.codewizards.meshify_chat.ui.signup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.ui.home.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText mUserName;
    @BindView(R.id.register_button)
    Button mRegisterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mUserName.setText(Build.MANUFACTURER + " " + Build.MODEL);
        this.mUserName.setEnabled(true);
        this.mRegisterButton.setEnabled(true);
    }

    @OnClick({R.id.register_button})
    public void attemptRegister(View v) {
        SignupActivity signupActivity = SignupActivity.this;
        MeshifySession.setSession(signupActivity, mUserName.getText().toString());
        sendUserToMainActivity();
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }


}