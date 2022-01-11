package com.codewizards.meshify_chat.ui.chat;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.adapters.ChatMessageAdapter;
import com.codewizards.meshify_chat.models.MeshifyFile;
import com.codewizards.meshify_chat.models.Message;
import com.codewizards.meshify_chat.util.Constants;
import com.github.clans.fab.FloatingActionButton;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

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

    @BindView(R.id.fabImage)
    protected FloatingActionButton fabImage;

    @BindView(R.id.fabGif)
    protected FloatingActionButton fabGif;

    ChatMessageAdapter chatMessageAdapter = new ChatMessageAdapter(new ArrayList<Message>());

    private ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;

    private String deviceName;

    private boolean lastSeen;

    private String deviceId;

    private final BroadcastReceiver messageBroadcastReceiver = new MessageBroadcastReceiver();

    public void pushMessageToView(Message message) {
        this.chatMessageAdapter.addMessage(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CHAT_MESSAGE_RECEIVED);
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
        messagesRecyclerView.setAdapter(chatMessageAdapter);

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

            HashMap<String, Object> content = new HashMap<>();
            content.put(Constants.PAYLOAD_TEXT, messageString);


            com.codewizards.meshify.client.Message.Builder builder = new com.codewizards.meshify.client.Message.Builder();
            builder.setContent(content).setReceiverId(deviceId);
            com.codewizards.meshify.client.Message message2 = builder.build();

            Meshify.sendMessage(message2, ConfigProfile.valueOf(this.sharedPreferences.getString(Constants.PREFS_CONFIG_PROFILE, "Default")));

            message.setUuid(message2.getUuid());
            chatMessageAdapter.addMessage(message);
        }
    }

    @OnClick({R.id.fabImage})
    public void onImageSend(View v) {

        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(Constants.FILE_PICKER_REQUEST_CODE)
                // Entry point path (user will start from it)
                .withPath(Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera")
                // Want to choose only jpg images
                .withFilter(Pattern.compile(".*\\.(jpg|jpeg)$"))
                .withHiddenFiles(true) // Show hidden files and folders
                .start();

    }

    @OnClick({R.id.fabGif})
    public void onGIFSend(View v) {

        Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.FILE_PICKER_REQUEST_CODE && data != null) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.i(TAG, "onActivityResult: file path " + filePath);
            File file = new File(filePath);
            byte fileContent[] = new byte[(int) file.length()];

            try {
                FileInputStream fin = new FileInputStream(file);
                fin.read(fileContent);
                HashMap<String, Object> content = new HashMap<>();
                content.put("file", file.getName());

                com.codewizards.meshify.client.Message.Builder builder = new com.codewizards.meshify.client.Message.Builder();
                com.codewizards.meshify.client.Message message = builder.setReceiverId(deviceId).setContent(content).setData(fileContent).build();
                message.setUuid(Meshify.sendMessage(message));

                //TODO - Add message to adapter

                progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle(file.getName());
                progressDialog.setMax(100);
                progressDialog.setProgress(1);
                progressDialog.setCancelable(true);
                progressDialog.show();

            } catch (IOException e) {
                e.printStackTrace();
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
                    com.codewizards.meshify.client.Message message = com.codewizards.meshify.client.Message.create(extras.getString(Constants.MESSAGE));

                    if (message.getContent().get("file") != null){

                        Message message2 = new Message(extras.getString(Constants.MESSAGE_UUID), (String) message.getContent().get("file"), deviceId, Meshify.getInstance().getMeshifyClient().getUserUuid());
                        message2.setDirection(Message.INCOMING_IMAGE);
                        ChatActivity.this.pushMessageToView(message2);

                    } else{

                        Message message2 = new Message(extras.getString(Constants.MESSAGE_UUID), (String) message.getContent().get("text"), deviceId, Meshify.getInstance().getMeshifyClient().getUserUuid());
                        message2.setDirection(Message.INCOMING_MESSAGE);
                        ChatActivity.this.pushMessageToView(message2);

                    }
                }
            }
        }
    }
}