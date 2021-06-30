package com.codewizards.meshify_chat.ux.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.entities.Message;
import com.codewizards.meshify_chat.ux.adapter.MessageAdapter;
import com.codewizards.meshify_chat.ux.home.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.codewizards.meshify_chat.ux.home.MainActivity.INTENT_EXTRA_NAME;
import static com.codewizards.meshify_chat.ux.home.MainActivity.INTENT_EXTRA_UUID;
import static com.codewizards.meshify_chat.ux.home.MainActivity.PAYLOAD_TEXT;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.txtMessage)
    EditText txtMessage;
    MessageAdapter adapter = new MessageAdapter(new ArrayList<Message>());
    private String deviceName;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        deviceName = getIntent().getStringExtra(INTENT_EXTRA_NAME);
        deviceId = getIntent().getStringExtra(INTENT_EXTRA_UUID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(deviceName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Message message = new Message(intent.getStringExtra(MainActivity.INTENT_EXTRA_MSG));
                        message.setDirection(Message.INCOMING_MESSAGE);
                        adapter.addMessage(message);
                    }
                }, new IntentFilter(deviceId));

        RecyclerView messagesRecyclerView = findViewById(R.id.messages);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        messagesRecyclerView.setAdapter(adapter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @OnClick({R.id.btnSend})
    public void onMessageSend(View v) {

        String messageString = txtMessage.getText().toString();
        if (messageString.trim().length() > 0) {

            txtMessage.setText("");

            Message message = new Message(messageString);
            message.setDirection(Message.OUTGOING_MESSAGE);
            adapter.addMessage(message);

            HashMap<String, Object> content = new HashMap<>();
            content.put(PAYLOAD_TEXT, messageString);

            com.codewizards.meshify.client.Message.Builder builder = new com.codewizards.meshify.client.Message.Builder();
            builder.setContent(content).setReceiverId(deviceId);

            Meshify.sendMessage(builder.build(), ConfigProfile.Default);

        }
    }
}