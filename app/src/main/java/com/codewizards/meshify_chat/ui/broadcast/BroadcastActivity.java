package com.codewizards.meshify_chat.ui.broadcast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.codewizards.meshify.logs.Log;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.util.Constants;
import com.github.clans.fab.FloatingActionButton;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class BroadcastActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][BroadcastActivity]";

    @BindView(R.id.broadcast_toolbar)
    Toolbar toolbar;
    @BindView(R.id.fabText)
    protected FloatingActionButton fabText;
    @BindView(R.id.txtChatLine)
    protected EditText chatLine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        ButterKnife.bind(this);

        setSupportActionBar(this.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        this.fabText.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.txtChatLine)
    public void onTextChanged(CharSequence charSequence) {
        if (charSequence.length() == 0) {
            this.fabText.setVisibility(View.GONE);
        } else {
            this.fabText.setVisibility(View.VISIBLE);
        }

    }


}