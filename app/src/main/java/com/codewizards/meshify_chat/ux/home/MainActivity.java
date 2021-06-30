package com.codewizards.meshify_chat.ux.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Constants;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.client.ConnectionListener;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify_chat.BuildConfig;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.entities.Neighbor;
import com.codewizards.meshify_chat.ux.adapter.NeighborAdapter;
import com.codewizards.meshify_chat.ux.chat.ChatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "[Meshify][MainActivity]";

    public static final String INTENT_EXTRA_NAME = "deviceName";
    public static final String INTENT_EXTRA_UUID = "deviceUuid";
    public static final String PAYLOAD_TEXT = "text";
    public static final String INTENT_EXTRA_MSG  = "message";

    NeighborAdapter adapter = new NeighborAdapter(new ArrayList<>());

    ProgressDialog progressDialog;

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            super.onMessageReceived(message);

            String msg = (String) message.getContent().get("text");
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(message.getSenderId()).putExtra(INTENT_EXTRA_MSG, msg));
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
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }

        @Override
        public void onStartError(String message, int errorCode) {
            super.onStartError(message, errorCode);
            progressDialog.dismiss();

            if (errorCode == Constants.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        @Override
        public void onDeviceConnected(Device device, Session session) {
            super.onDeviceConnected(device, session);
            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName(), device);
            neighbor.setNearby(true);
            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
            adapter.addNeighbor(neighbor);

            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), "Neighbor found" + neighbor.getDeviceName(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDeviceBlackListed(Device device) {
            super.onDeviceBlackListed(device);
            adapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Blacklisted " + device.getDeviceName() , Toast.LENGTH_LONG).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            super.onDeviceLost(device);
            adapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Lost " + device.getDeviceName(), Toast.LENGTH_LONG).show();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Configure the Toolbar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        RecyclerView recyclerView = findViewById(R.id.neighbor_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(MainActivity.this);

        Meshify.debug = BuildConfig.DEBUG;

        Meshify.initialize(getApplicationContext());

        startMeshify();

        adapter.setOnItemClickListener(new NeighborAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Neighbor neighbor) {
                startActivity(new Intent(getApplicationContext(), ChatActivity.class)
                        .putExtra(INTENT_EXTRA_NAME, neighbor.getDeviceName())
                        .putExtra(INTENT_EXTRA_UUID, neighbor.getUuid()));
            }
        });

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
}
