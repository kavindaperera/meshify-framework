package com.codewizards.meshify_chat.ui.home;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConnectionListener;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify_chat.BuildConfig;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.adapters.NeighborAdapter;
import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.permissions.RationaleDialog;
import com.codewizards.meshify_chat.service.MeshifyNotifications;
import com.codewizards.meshify_chat.service.MeshifyService;
import com.codewizards.meshify_chat.ui.about.AboutActivity;
import com.codewizards.meshify_chat.ui.avatar.ChooseAvatarActivity;
import com.codewizards.meshify_chat.ui.broadcast.BroadcastActivity;
import com.codewizards.meshify_chat.ui.chat.ChatActivity;
import com.codewizards.meshify_chat.ui.settings.SettingsActivity;
import com.codewizards.meshify_chat.ui.splash.SplashActivity;
import com.codewizards.meshify_chat.util.Constants;
import com.codewizards.meshify_chat.util.MeshifyUtils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.codewizards.meshify_chat.util.Constants.BROADCAST_CHAT;
import static com.codewizards.meshify_chat.util.Constants.PAYLOAD_DEVICE_NAME;
import static com.codewizards.meshify_chat.util.Constants.PAYLOAD_TEXT;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][MainActivity]";

    NeighborAdapter adapter;
    SharedPreferences sharedPreferences;

    private String username;

    private ProgressBar mProgressBar;

    /* ViewModel */
    private MainViewModel mainViewModel;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {

            if (message.getContent().get(PAYLOAD_DEVICE_NAME) != null) {
                String senderId = message.getSenderId();
                String userName = (String) message.getContent().get(PAYLOAD_DEVICE_NAME);
                mainViewModel.updateNameByUuid(senderId, userName);

                hideProgressBar();

            } else {


                //TODO- Remove later
                Neighbor neighbor = adapter.getNeighborById(message.getSenderId());
                String nName = "Unknown User";
                if (neighbor != null) {
                    nName = neighbor.getDeviceName();
                }

                MeshifyNotifications.getInstance().createChatNotification(message.getSenderId(), message, nName);

                mainViewModel.updateLastSeen(message.getSenderId(), String.valueOf(System.currentTimeMillis()));

                Bundle prepareMessageBundle = MeshifyNotifications.prepareMessageBundle(message, nName);
                LocalBroadcastManager
                        .getInstance(getBaseContext())
                        .sendBroadcast(new Intent().setAction(Constants.CHAT_MESSAGE_RECEIVED).putExtras(prepareMessageBundle));

            }
        }

        @Override
        public void onBroadcastMessageReceived(Message message) {
            String Msg = (String) message.getContent().get(PAYLOAD_TEXT);
            String deviceName  = (String) message.getContent().get(PAYLOAD_DEVICE_NAME);
            Log.i(TAG, "Incoming broadcast message: " + Msg + " from " + deviceName);

            Bundle prepareMessageBundle = MeshifyNotifications.prepareMessageBundle(message, Constants.BROADCAST_CHAT);
            LocalBroadcastManager
                    .getInstance(getBaseContext())
                    .sendBroadcast(new Intent().setAction(Constants.BROADCAST_CHAT_MESSAGE_RECEIVED).putExtras(prepareMessageBundle));

        }

        @Override
        public void onMessageSent(String messageId) {
            Log.i(TAG, "onMessageSent: " + messageId);
        }

        @Override
        public void onMessageFailed(Message message, MessageException exception) {
            Log.e(TAG, "onMessageFailed:" + exception.getMessage());
            Toast.makeText(getApplicationContext(), exception.getMessage() , Toast.LENGTH_SHORT).show();
        }
    };

    ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void onStarted() {
            Log.d(TAG, "onStarted:");
        }


        @Override
        public void onDeviceDiscovered(Device device) {
            Toast.makeText(getApplicationContext(), "Device Discovered " + device.getDeviceName() , Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Device Discovered " +  device.toString());
        }

        @Override
        public void onStartError(String message, int errorCode) {
            hideProgressBar();
            if (errorCode == com.codewizards.meshify.client.Constants.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            if (errorCode == com.codewizards.meshify.client.Constants.LOCATION_SERVICES_DISABLED) {

                RationaleDialog.createFor(MainActivity.this, "Meshify needs you to turn on location services in order to connect with friends", R.drawable.ic_location_outline_32)
                        .setNegativeButton(R.string.Permissions_not_now, (dialog, whichButton) -> finish())
                        .setPositiveButton(R.string.Permissions_continue, (dialog, whichButton) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        })
                        .setCancelable(false)
                        .show()
                        .getWindow()
                        .setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            }
        }


        @Override
        public void onDeviceConnected(Device device, Session session) {
            Log.e(TAG, "Device Connected " +  device.toString());
            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName());
            neighbor.setNearby(true);
            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
            neighbor.setDevice(device);
            neighbor.setLastSeen(String.valueOf(System.currentTimeMillis()));

            mainViewModel.insert(neighbor);
            mainViewModel.updateNearby(device.getUserId(), true);
            mainViewModel.updateLastSeen(device.getUserId(), String.valueOf(System.currentTimeMillis()));

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
            Toast.makeText(getApplicationContext(), "Blacklisted " + device.getDeviceName() , Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            mainViewModel.updateNearby(device.getUserId(), false);
            Toast.makeText(getApplicationContext(), "Lost " + device.getDeviceName(), Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MeshifySession.isLoggedIn()) {
            init(savedInstanceState);
            return;
        }

        showSplashActivity();

    }



    @OnClick(R.id.fab)
    public void newConversation(View v) {
        startActivity(new Intent(this, BroadcastActivity.class));
    }

    private void showSplashActivity() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void init(Bundle bundle) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.fab.setVisibility(View.INVISIBLE);

        this.sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        /*Configure the Toolbar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /*RecyclerView*/
        RecyclerView recyclerView = findViewById(R.id.neighbor_list);
        adapter = new NeighborAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        /*ViewModel*/
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getAllNeighbors().observe(this, neighbors -> {
              adapter.submitList(neighbors);
        });

        mProgressBar = findViewById(R.id.progress_bar);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { // swipe to delete
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Neighbor n1  = adapter.getNeighborAt(viewHolder.getAdapterPosition());
                mainViewModel.delete(n1);

                Snackbar.make(recyclerView, n1.getDeviceName() + " Achieved.", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            mainViewModel.insert(n1);
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_archive_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.verify_error))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }).attachToRecyclerView(recyclerView);

        Meshify.debug = BuildConfig.DEBUG; // remove in production
        Meshify.initialize(getApplicationContext());

        startMeshify();

        adapter.setOnItemClickListener(neighbor -> startActivity(new Intent(getApplicationContext(), ChatActivity.class)
                .putExtra(Constants.INTENT_EXTRA_NAME, neighbor.getDeviceName())
                .putExtra(Constants.INTENT_EXTRA_LAST_SEEN, neighbor.isNearby())
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
    protected void onDestroy() {
        super.onDestroy();
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
                startActivity(new Intent(getBaseContext(), BroadcastActivity.class)
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
        builder.setAutoConnect(true);

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
