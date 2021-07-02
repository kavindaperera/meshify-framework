package com.codewizards.meshify_chat.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.adapters.NeighborAdapter;
import com.codewizards.meshify_chat.ui.chat.ChatActivity;
import com.codewizards.meshify_chat.utils.Constants;

import java.util.HashMap;
import java.util.List;

import static com.codewizards.meshify_chat.utils.Constants.PAYLOAD_DEVICE_NAME;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][MainActivity]";

    NeighborAdapter adapter;
    SharedPreferences sharedPreferences;

    private String username;

    private ProgressBar mProgressBar;

    private MainViewModel mainViewModel;

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            super.onMessageReceived(message);
            if (message.getContent().get(PAYLOAD_DEVICE_NAME) != null) {
                Neighbor neighbor = new Neighbor(message.getSenderId(),(String) message.getContent().get(PAYLOAD_DEVICE_NAME));
                neighbor.setNearby(true);
                neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
                adapter.addNeighbor(neighbor);

                hideProgressBar();

                Toast.makeText(getApplicationContext(), "Neighbor Found: " + neighbor.getDeviceName(), Toast.LENGTH_SHORT).show();

            } else {
                String msg = (String) message.getContent().get("text");
                LocalBroadcastManager
                        .getInstance(getBaseContext())
                        .sendBroadcast(new Intent(message.getSenderId()).putExtra(Constants.INTENT_EXTRA_MSG, msg));
            }
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
//            showProgressBar();
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
//            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName());
//            neighbor.setNearby(true);
//            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
//            adapter.addNeighbor(neighbor);

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
            Toast.makeText(getApplicationContext(), "Blacklisted " + device.getDeviceName() , Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            super.onDeviceLost(device);
            adapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Lost " + device.getDeviceName(), Toast.LENGTH_SHORT).show();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * start meshify
     */
    private void startMeshify() {

        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);

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
