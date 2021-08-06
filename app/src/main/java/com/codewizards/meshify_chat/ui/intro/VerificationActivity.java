package com.codewizards.meshify_chat.ui.intro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codewizards.meshify_chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationActivity extends AppCompatActivity {

    @BindView(R.id.phone_number_input)
    EditText mPhoneEditText;

    @BindView(R.id.phone_number_error)
    TextView mErrorText;

    @BindView(R.id.btn_submit)
    Button mPhoneSubmitBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_submit})
    public void onRequestVerification(View v) {
        setResult(-1);
        finish();
    }

    @OnClick({R.id.txt_cancel})
    public void onCancel() {
        setResult(0);
        finish();
    }

}