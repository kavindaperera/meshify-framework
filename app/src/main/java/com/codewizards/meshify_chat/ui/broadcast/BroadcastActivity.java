package com.codewizards.meshify_chat.ui.broadcast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.adapters.BroadcastMessageAdapter;
import com.codewizards.meshify_chat.models.Message;
import com.codewizards.meshify_chat.util.Constants;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;

public class BroadcastActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][BroadcastActivity]";

    private final BroadcastReceiver messageBroadcastReceiver = new BroadcastActivity.MessageBroadcastReceiver();

    @BindView(R.id.fabText)
    protected FloatingActionButton fabText;

    @BindView(R.id.txtChatLine)
    protected EditText chatLine;

    @BindView(R.id.broadcast_toolbar)
    Toolbar toolbar;

    SharedPreferences sharedPreferences;

    BroadcastMessageAdapter messageAdapter = new BroadcastMessageAdapter(new ArrayList<Message>());

    private String deviceId;

    public void pushMessageToView(Message message) {
        this.messageAdapter.addMessage(message);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_CHAT_MESSAGE_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.messageBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        ButterKnife.bind(this);

        this.sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        deviceId = getIntent().getStringExtra(Constants.INTENT_EXTRA_UUID);

        setSupportActionBar(this.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        this.fabText.setVisibility(View.INVISIBLE);

        RecyclerView messagesRecyclerView = findViewById(R.id.messages_recyclerview);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

    }

    @OnTextChanged(R.id.txtChatLine)
    public void onTextChanged(CharSequence charSequence) {
        if (charSequence.length() == 0) {
            this.fabText.hide(true);
        } else {
            this.fabText.show(true);
        }
    }

    @OnLongClick(R.id.txtChatLine)
    public boolean onChatLineLongClick() {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Service.CLIPBOARD_SERVICE);
        if (clipboardManager.getPrimaryClip() == null || clipboardManager.getPrimaryClip().getItemAt(0) == null) {
            return true;
        }
        ClipData.Item itemAt = clipboardManager.getPrimaryClip().getItemAt(0);
        this.chatLine.append((itemAt.getText() != null ? itemAt.getText().toString() : ""));
        return true;
    }

    @OnClick({R.id.fabText})
    public void onMessageSend(View v) {

        String messageString = chatLine.getText().toString();
        if (messageString.trim().length() > 0) {

            chatLine.setText("");

            Message message = new Message(messageString, Meshify.getInstance().getMeshifyClient().getUserUuid(), deviceId);
            message.setDirection(Message.OUTGOING_BROADCAST_MESSAGE);

            HashMap<String, Object> content = new HashMap<>();
            content.put(Constants.PAYLOAD_TEXT, messageString);

            if (deviceId.equals(Constants.BROADCAST_CHAT)) {

                String username = sharedPreferences.getString(Constants.PREFS_USERNAME, null);
                if (username == null) {
                    username = Build.MANUFACTURER + " " + Build.MODEL;
                }
                content.put(Constants.PAYLOAD_DEVICE_NAME, username);

                com.codewizards.meshify.client.Message.Builder builder = new com.codewizards.meshify.client.Message.Builder();
                builder.setContent(content);
                com.codewizards.meshify.client.Message message1 = builder.build();
                Meshify.sendBroadcastMessage(message1, ConfigProfile.Default);

                message.setUuid(message1.getUuid());
                messageAdapter.addMessage(message);

            }
        }
    }

    class MessageBroadcastReceiver extends BroadcastReceiver {
        MessageBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_CHAT_MESSAGE_RECEIVED)) {
                Bundle extras = intent.getExtras();
                String string = extras.getString(Constants.OTHER_USER_ID, "");
                com.codewizards.meshify.client.Message message = com.codewizards.meshify.client.Message.create(extras.getString(Constants.MESSAGE));
                Message message2 = new Message(
                        extras.getString(Constants.MESSAGE_UUID),
                        (String) message.getContent().get("text"),
                        (String) message.getSenderId(),
                        Meshify.getInstance().getMeshifyClient().getUserUuid(),
                        (String) message.getContent().get("device_name")
                );
                message2.setDirection(Message.INCOMING_BROADCAST__MESSAGE);
                BroadcastActivity.this.pushMessageToView(message2);
            }
        }
    }
}



