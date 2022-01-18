package com.codewizards.meshifyanalyzer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.ConnectionListener;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.framework.expections.MessageException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "[Meshify][MainActivity]";

    @BindView(R.id.devices_recycler_view)
    RecyclerView devicesRecyclerView;
    DevicesAdapter devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        // initialize the DevicesAdapter and the RecyclerView
        devicesAdapter = new DevicesAdapter();
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        // check that we have Location permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeMeshify();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Meshify.stop();
    }

    private void initializeMeshify() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Meshify.debug = BuildConfig.DEBUG;

        Meshify.initialize(getApplicationContext());

        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);
        builder.setAutoConnect(false);
        builder.setConfigProfile(ConfigProfile.NoForwarding);

        Meshify.start(messageListener, connectionListener, builder.build());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeMeshify();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start devices discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onDeviceConnected(Device device, Session session) {
            Log.i(TAG, "Device found: " + device.getUserId() + " isClient: " + session.isClient());

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String device2 = timestamp + " | Device found: " + device.getDeviceName() + " isClient: " + session.isClient() ;
            devicesAdapter.addDevice(device2);

            devicesRecyclerView.post(() -> {
                // Call smooth scroll
                devicesRecyclerView.smoothScrollToPosition(devicesAdapter.getItemCount() - 1);
            });

            timerHello(Constants.HELLO_PACKET_INTERVAL, device, session.isClient());
        }

        @Override
        public void onDeviceLost(Device device) {
            Log.w(TAG, "Device lost: " + device.getUserId());

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String device2 = timestamp + " | Device lost: " + device.getDeviceName() ;
            devicesAdapter.addDevice(device2);

            devicesRecyclerView.post(() -> {
                // Call smooth scroll
                devicesRecyclerView.smoothScrollToPosition(devicesAdapter.getItemCount() - 1);
            });

        }


        @Override
        public void onStarted() {
            super.onStarted();
            Log.i(TAG, "onStarted: Meshify started");
        }

        @Override
        public void onStartError(String s, int i) {
            super.onStartError(s, i);
            Log.e(TAG, "onStartError: " + s + " " + i);
        }

    };

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String device = timestamp + " | Hello from " + message.getContent().get("manufacturer ") + " " + message.getContent().get("model") ;
            Log.e(TAG, "Message Received: " + message.getSenderId() + ", content: " + message.getContent());
            devicesAdapter.addDevice(device);

            devicesRecyclerView.post(() -> {
                // Call smooth scroll
                devicesRecyclerView.smoothScrollToPosition(devicesAdapter.getItemCount() - 1);
            });

        }

        @Override
        public void onMessageFailed(Message message, MessageException e) {
            Log.e(TAG, "Message failed", e);
        }

    };




    private void timerHello(final int time, Device device, boolean b1) {
        Timer timerHelloPackets = new Timer();
        timerHelloPackets.schedule(new TimerTask() {
            @Override
            public void run() {
                if (b1) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("manufacturer ", Build.MANUFACTURER);
                    data.put("model", Build.MODEL);
                    data.put("time", new java.sql.Timestamp(System.currentTimeMillis()).getTime());
                    device.sendMessage(data);
                    Log.d(TAG, "Hello message sent!");
                    timerHello(time, device, b1);
                }
            }
        }, time);
    }

    public class DevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
        ArrayList<String> devices;
        DevicesAdapter() {
            devices = new ArrayList<>();
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        boolean addDevice(String device) {
//            if (!devices.contains(device)) {
                devices.add(device);
                notifyItemInserted(devices.size() - 1);
                return true;
//            }

//            return false;
        }

        void removeDevice(Device device) {
            int position = devices.indexOf(device);
            if (position > -1) {
                devices.remove(position);
                notifyItemRemoved(position);
            }
        }

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View deviceView = LayoutInflater.from(viewGroup.getContext()).
                    inflate((R.layout.device_row), viewGroup, false);
            return new DeviceViewHolder(deviceView);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int position) {
            deviceViewHolder.setDevice(devices.get(position));
        }
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_device)
        TextView deviceView;

        DeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void setDevice(String device) {
            deviceView.setText(device);
        }
    }
}