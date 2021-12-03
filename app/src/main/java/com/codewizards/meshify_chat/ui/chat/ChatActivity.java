package com.codewizards.meshify_chat.ui.chat;

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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.adapters.MessageAdapter;
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


public class ChatActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][ChatActivity]";

    @BindView(R.id.txtChatLine)
    protected EditText chatLine;

    @BindView(R.id.fabText)
    protected FloatingActionButton fabText;

    MessageAdapter messageAdapter = new MessageAdapter(new ArrayList<Message>());
    SharedPreferences sharedPreferences;
    private String deviceName;
    private boolean lastSeen;
    private String deviceId;
    private final BroadcastReceiver messageBroadcastReceiver = new MessageBroadcastReceiver();

    public void pushMessageToView(Message message) {
        this.messageAdapter.addMessage(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CHAT_MESSAGE_RECEIVED);
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
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        this.sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        deviceName = getIntent().getStringExtra(Constants.INTENT_EXTRA_NAME);
        lastSeen = getIntent().getBooleanExtra(Constants.INTENT_EXTRA_LAST_SEEN, false);
        deviceId = getIntent().getStringExtra(Constants.INTENT_EXTRA_UUID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(deviceName);
            if (!deviceId.equals(Constants.BROADCAST_CHAT)) {
                actionBar.setSubtitle(lastSeen ? "Nearby" : "Not in Range");
            } else {
                actionBar.setSubtitle("");
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.fabText.setVisibility(View.INVISIBLE);

        RecyclerView messagesRecyclerView = findViewById(R.id.messages);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
            message.setDirection(Message.OUTGOING_MESSAGE);
            messageAdapter.addMessage(message);

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
                Meshify.sendBroadcastMessage(builder.build(), ConfigProfile.Default);


            } else {
                com.codewizards.meshify.client.Message.Builder builder = new com.codewizards.meshify.client.Message.Builder();
                builder.setContent(content).setReceiverId(deviceId);
                Meshify.sendMessage(builder.build(), ConfigProfile.valueOf(this.sharedPreferences.getString(Constants.PREFS_CONFIG_PROFILE, "Default")));
            }
        }
    }

    class MessageBroadcastReceiver extends BroadcastReceiver {
        MessageBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.CHAT_MESSAGE_RECEIVED)) {
                Bundle extras = intent.getExtras();
                String string = extras.getString(Constants.OTHER_USER_ID, "");
                if (ChatActivity.this.deviceId != null && ChatActivity.this.deviceId.equals(string)) {
                    Message message = new Message(extras.getString(Constants.MESSAGE), deviceId, Meshify.getInstance().getMeshifyClient().getUserUuid());
                    message.setDirection(Message.INCOMING_MESSAGE);
                    ChatActivity.this.pushMessageToView(message);
                }
            } else if (intent.getAction().equals(Constants.BROADCAST_CHAT_MESSAGE_RECEIVED)) {
                Bundle extras = intent.getExtras();
                String string = extras.getString(Constants.OTHER_USER_ID, "");

                Message message = new Message(extras.getString(Constants.MESSAGE), deviceId, Meshify.getInstance().getMeshifyClient().getUserUuid());
                message.setDirection(Message.INCOMING_MESSAGE);
                ChatActivity.this.pushMessageToView(message);

            }
        }
    }
}