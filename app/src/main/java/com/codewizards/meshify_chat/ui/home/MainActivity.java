package com.codewizards.meshify_chat.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.client.ConnectionListener;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify_chat.BuildConfig;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.adapters.NeighborAdapter;
import com.codewizards.meshify_chat.service.MeshifyNotifications;
import com.codewizards.meshify_chat.service.MeshifyService;
import com.codewizards.meshify_chat.ui.about.AboutActivity;
import com.codewizards.meshify_chat.ui.chat.ChatActivity;
import com.codewizards.meshify_chat.ui.settings.SettingsActivity;
import com.codewizards.meshify_chat.ui.splash.SplashActivity;
import com.codewizards.meshify_chat.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.codewizards.meshify_chat.util.Constants.BROADCAST_CHAT;
import static com.codewizards.meshify_chat.util.Constants.PAYLOAD_DEVICE_NAME;
import static com.codewizards.meshify_chat.util.Constants.PAYLOAD_TEXT;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][MainActivity]";

    NeighborAdapter adapter;
    SharedPreferences sharedPreferences;

    private String username;

    private ProgressBar mProgressBar;

    private MainViewModel mainViewModel;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            super.onMessageReceived(message);

            if (message.getContent().get(PAYLOAD_DEVICE_NAME) != null) {
//                Neighbor neighbor = new Neighbor(message.getSenderId(),(String) message.getContent().get(PAYLOAD_DEVICE_NAME));
//                neighbor.setNearby(true);
//                neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
//                adapter.addNeighbor(neighbor);
                String senderId = message.getSenderId();
                String userName = (String) message.getContent().get(PAYLOAD_DEVICE_NAME);
                adapter.updateNeighbor(senderId, userName);

                hideProgressBar();

            } else {
                String text = (String) message.getContent().get("text");
                LocalBroadcastManager
                        .getInstance(getBaseContext())
                        .sendBroadcast(new Intent(message.getSenderId()).putExtra(Constants.INTENT_EXTRA_MSG, text));

                MeshifyNotifications.getInstance().createChatNotification(message, text); //Remove

            }
        }

        @Override
        public void onBroadcastMessageReceived(Message message) {
            super.onBroadcastMessageReceived(message);
            String Msg = (String) message.getContent().get(PAYLOAD_TEXT);
            String deviceName  = (String) message.getContent().get(PAYLOAD_DEVICE_NAME);

            Log.e(TAG, "Incoming broadcast message: " + Msg + " from " + deviceName);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                    new Intent(BROADCAST_CHAT)
                            .putExtra(Constants.INTENT_EXTRA_NAME, deviceName)
                            .putExtra(Constants.INTENT_EXTRA_MSG,  Msg));
        }

        @Override
        public void onMessageFailed(Message message, MessageException exception) {
            super.onMessageFailed(message, exception);
            Log.e(TAG, "onMessageFailed:" + exception.getMessage());
            Toast.makeText(getApplicationContext(), exception.getMessage() , Toast.LENGTH_SHORT).show();
        }
    };

    ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void onStarted() {
            super.onStarted();
            Log.d(TAG, "onStarted:");
//            showProgressBar();
        }


        @Override
        public void onDeviceDiscovered(Device device) {
            super.onDeviceDiscovered(device);
            Toast.makeText(getApplicationContext(), "Device Discovered " + device.getDeviceName() , Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Device Discovered " +  device.toString());
        }

        @Override
        public void onStartError(String message, int errorCode) {
            super.onStartError(message, errorCode);
            hideProgressBar();
            if (errorCode == com.codewizards.meshify.client.Constants.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        @Override
        public void onDeviceConnected(Device device, Session session) {
            super.onDeviceConnected(device, session);
            Log.e(TAG, "Device Connected " +  device.toString());
            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName());
            neighbor.setNearby(true);
            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
            neighbor.setDevice(device);
            adapter.addNeighbor(neighbor);

            //send username and phone number
            username = sharedPreferences.getString(Constants.PREFS_USERNAME, null);
            if (username == null) {
                username = Build.MANUFACTURER + " " + Build.MODEL;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.PAYLOAD_DEVICE_NAME, username);
            device.sendMessage(map);

            showProgressBar();

        }

        @Override
        public void onDeviceBlackListed(Device device) {
            super.onDeviceBlackListed(device);
            adapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Blacklisted " + device.getDeviceAddress() , Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            super.onDeviceLost(device);
            adapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Lost " + device.getDeviceAddress(), Toast.LENGTH_SHORT).show();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ContactUtils contactUtils = new ContactUtils();
//        try {
//            Log.e(TAG, contactUtils.getPhonesNamesAndLabels("").toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Cursor c = getContentResolver().query(
//                ContactsContract.RawContacts.CONTENT_URI,
//                new String[] { ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY },
//                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
//                new String[] { "com.whatsapp" },
//                null);
//
//        ArrayList<String> myWhatsappContacts = new ArrayList<String>();
//        int contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
//        while (c.moveToNext())
//        {
//            // You can also read RawContacts.CONTACT_ID to read the
//            // ContactsContract.Contacts table or any of the other related ones.
//            myWhatsappContacts.add(c.getString(contactNameColumn));
//        }
//
//        Log.e(TAG, myWhatsappContacts.toString());

//        String accountType = "com.codewizards.meshify";
//        String accountName = "Meshify";
//
//        ContentValues values = new ContentValues();
//        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType);
//        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, accountName);
//        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
//        long rawContactId = ContentUris.parseId(rawContactUri);
//
//        values.clear();
//        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
//        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
//        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "Mike Sullivan");
//        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);


        if (MeshifySession.isLoggedIn()) {
            init(savedInstanceState);
            return;
        }

        showSplashActivity();

    }

    @OnClick(R.id.fab)
    public void newConversation(View v) {
        Log.e(TAG, "New Conversation Activity");
    }

    private void showSplashActivity() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void init(Bundle bundle) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        this.sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        /*Configure the Toolbar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /*ViewModel*/
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.init();
        mainViewModel.getNeighbors().observe(this, new Observer<List<Neighbor>>() {
            @Override
            public void onChanged(List<Neighbor> neighbors) {
                adapter.notifyDataSetChanged();
            }
        });

        mProgressBar = findViewById(R.id.progress_bar);

        RecyclerView recyclerView = findViewById(R.id.neighbor_list);
        adapter = new NeighborAdapter(this, mainViewModel.getNeighbors().getValue());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        Meshify.debug = BuildConfig.DEBUG;
        Meshify.initialize(getApplicationContext());

        startMeshify();

        adapter.setOnItemClickListener(neighbor -> startActivity(new Intent(getApplicationContext(), ChatActivity.class)
                .putExtra(Constants.INTENT_EXTRA_NAME, neighbor.getDeviceName())
                .putExtra(Constants.INTENT_EXTRA_UUID, neighbor.getUuid())));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, MeshifyService.class).setAction(Constants.MESHIFY_APP_BACKGROUND));
        } else {
            startService(new Intent(this, MeshifyService.class).setAction(Constants.MESHIFY_APP_BACKGROUND));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_settings:{
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.action_invite: {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, I'm using Meshify, Join me! \nDownload it here: https://www.meshify.xyz/");
                sendIntent.setType("text/plain");

                // Show the Sharesheet
                startActivity(Intent.createChooser(sendIntent, null));
                break;
            }
            case R.id.action_broadcast: {
                startActivity(new Intent(getBaseContext(), ChatActivity.class)
                        .putExtra(Constants.INTENT_EXTRA_NAME, BROADCAST_CHAT)
                        .putExtra(Constants.INTENT_EXTRA_UUID, BROADCAST_CHAT));
                return true;
            }
            case R.id.action_about: {
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * start meshify
     */
    private void startMeshify() {

        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);
        builder.setVerified(MeshifySession.isVerified());
        builder.setAutoConnect(false);

        Meshify.start(messageListener, connectionListener, builder.build());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMeshify(); //start meshify again after permission is granted
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions is needed to start discovery!", Toast.LENGTH_SHORT).show(); //close app on deny permissions
            finish();
        }
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mProgressBar.setVisibility(View.GONE);
    }
}
