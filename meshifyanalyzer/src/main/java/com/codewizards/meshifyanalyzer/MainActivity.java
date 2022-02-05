package com.codewizards.meshifyanalyzer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.ConfigProfile;
import com.codewizards.meshify.api.ConnectionListener;
import com.codewizards.meshify.api.Device;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.api.MessageListener;
import com.codewizards.meshify.api.Session;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.MeshifyLogger;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

import static com.codewizards.meshifyanalyzer.Constants.RTT_PACKET_INTERVAL;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "[Meshify][MainActivity]";

    ArrayList<SelectedDevice> listOfDevices = new ArrayList<>();

    private CustomAdapter dataAdapter;

    Timer timerHelloPackets;

    Timer timerRTTPackets;

    EditText editTextSize;

    Boolean isRTStarter = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
            return;
        }

        // check that we have Location permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeMeshify();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }


        Button btnStartTest = findViewById(R.id.button_start_test);
        Button btnStopTest = findViewById(R.id.button_stop);
        Button btnStop = findViewById(R.id.button_stop_meshify);
        Button btnStart = findViewById(R.id.button_start_meshify);
        Button btnDisconnect = findViewById(R.id.button_disconnect_device);
        Button btnStartRTTest = findViewById(R.id.button_start_rt_test);
        Button btnStopRTTest = findViewById(R.id.button_stop_rt_test);

        editTextSize = findViewById(R.id.editTextSize);

        editTextSize.setText("10");

        btnStartTest.setOnClickListener(v -> {

            if (dataAdapter == null) {
                Snackbar.make(v, "No Neighbors Found!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED)
                        .setAction("Action", null).show();
                return;
            }

            if (editTextSize.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter a Message Size!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<SelectedDevice> devices = dataAdapter.getDeviceList();
            for (int i = 0; i < devices.size(); i++) {
                SelectedDevice device = devices.get(i);

                Log.e(TAG,  device.toString() + " isSelected: " + device.isSelected());

                if (device.isSelected() ) {
                        timerHello(Constants.HELLO_PACKET_INTERVAL, device.device, true, Integer.parseInt(editTextSize.getText().toString()));
                        btnStartTest.setVisibility(View.GONE);
                }
            }

        });

        btnDisconnect.setOnClickListener(v -> {

            if (dataAdapter == null) {
                Snackbar.make(v, "No Neighbors Found!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED)
                        .setAction("Action", null).show();
                return;
            }

            ArrayList<SelectedDevice> devices = dataAdapter.getDeviceList();
            for (int i = 0; i < devices.size(); i++) {
                SelectedDevice device = devices.get(i);

                if (device.isSelected() ) {
                    Device device1 = device.device;
                    if (device1 != null) {
                        Meshify.getInstance().getMeshifyCore().disconnectDevice(device1);

                        Snackbar.make(v, "Disconnecting " + device1.getDeviceName() + "...", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED)
                                .setAction("Action", null).show();
                    }
                }
            }

        });

        btnStopTest.setOnClickListener(v -> {

            if (timerHelloPackets == null){
                Snackbar.make(v, "No Testing Schedule Found!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED)
                        .setAction("Action", null).show();
            } else {
                timerHelloPackets.cancel();
                Snackbar.make(v, "Scheduled Test Stopped!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN)
                        .setAction("Action", null).show();
                btnStartTest.setVisibility(View.VISIBLE);
            }

        });

        btnStop.setOnClickListener(v -> {
            Meshify.stop();
            Snackbar.make(v, "Stopping Meshify...", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED)
                    .setAction("Action", null).show();

            btnStart.setVisibility(View.VISIBLE);
        });

        btnStart.setOnClickListener(v -> {
            Snackbar.make(v, "Meshify Starting...", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN)
                    .setAction("Action", null).show();

            initializeMeshify();
        });


        btnStartRTTest.setOnClickListener(v -> {

            if (dataAdapter == null) {
                Snackbar.make(v, "No Neighbors Found!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED)
                        .setAction("Action", null).show();
                return;
            }

            if (editTextSize.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter a Message Size!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<SelectedDevice> devices = dataAdapter.getDeviceList();
            for (int i = 0; i < devices.size(); i++) {
                SelectedDevice device = devices.get(i);

                Log.e(TAG,  device.toString() + " isSelected: " + device.isSelected());

                if (device.isSelected() ) {
                    isRTStarter = true;
                    timerRTT(RTT_PACKET_INTERVAL, device.device, Integer.parseInt(editTextSize.getText().toString()));
                    btnStartRTTest.setVisibility(View.GONE);
                }
            }

        });

        btnStopRTTest.setOnClickListener(v -> {
            if (timerRTTPackets == null){
                Snackbar.make(v, "No RTT Schedule Found!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED)
                        .setAction("Action", null).show();
            } else {
                timerRTTPackets.cancel();
                isRTStarter = false;
                Snackbar.make(v, "Scheduled RTT Stopped!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN)
                        .setAction("Action", null).show();
                btnStartRTTest.setVisibility(View.VISIBLE);
            }
        });


        // Save Contacts

//        addTestDevicesForMiranda();

    }

    private void addTestDevicesForMiranda() {

        Device device1 = new Device("Elsa's Note 9","50:3D:C6:AB:52:2F", "83934c5a-6827-43ee-9631-a9ca95438ec7");
        Device device2 = new Device("Galaxy On7 Pro","30:6A:85:75:C5:49", "dc069d2c-0e98-4b8c-a601-a4abf8530e19");

        listOfDevices.add(new SelectedDevice(device1));
        listOfDevices.add(new SelectedDevice(device2));
        updateListView();

    }

    private void timerRTT(final int time, Device device, Integer size) {

        timerRTTPackets = new Timer();

        timerRTTPackets.schedule(new TimerTask() {
            @Override
            public void run() {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(Constants.RT_TEST_KEY, getAlphaNumericString(size));
                    device.sendMessage(data);
                    timerRTT(time, device, size);
            }
        }, time);
    }

    private void updateListView() {

        // Create an ArrayAdaptar from the String Array
        dataAdapter = new CustomAdapter(MainActivity.this, R.layout.device_row, listOfDevices);
        ListView listView = findViewById(R.id.listViewDiscoveredDevice);
        listView.setVisibility(View.VISIBLE);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });

    }

    // function to generate a random string of length n
    static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
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

        if (id == R.id.clear_logs) {
            MeshifyLogger.clearLogs();
            Toast.makeText(getApplicationContext(), "Log File Cleared", Toast.LENGTH_SHORT).show();
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

        Meshify.debug = BuildConfig.DEBUG;

        Meshify.initialize(getApplicationContext());

        MeshifyLogger.init(this.getBaseContext(), true);
        MeshifyLogger.startLogs();

        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);
        builder.setAutoConnect(false);
        builder.setConfigProfile(ConfigProfile.Default);

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
            Log.i(TAG, "Device Connected: " + device.getUserId() + " isClient: " + session.isClient());
            updateLog(Constants.NORMAL, "Device Connected: " + device.getDeviceName() + " - " + device.getUserId() + " | isClient: " + session.isClient()) ;

            if (listOfDevices.contains(new SelectedDevice(device))) {

                // Do nothing

            } else {

                listOfDevices.add(new SelectedDevice(device));
                updateListView();

            }


        }

        @Override
        public void onDeviceBlackListed(Device device) {

        }

        @Override
        public void onDeviceLost(Device device) {
            Log.w(TAG, "Device lost: " + device.getUserId());
            updateLog(Constants.ERROR, "Device lost: " + device.getDeviceName() + " - " + device.getUserId()) ;

        }

        @Override
        public void onIndirectDeviceDiscovered(Device device) {

        }


        @Override
        public void onStarted() {
            Log.i(TAG, "onStarted: Meshify started");
        }

        @Override
        public void onDeviceDiscovered(Device device) {

        }

        @Override
        public void onStartError(String s, int i) {
            Log.e(TAG, "onStartError: " + s + " " + i);
        }

    };

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            Log.e(TAG, "Message Received");

            if (message.getContent().containsKey(Constants.RT_TEST_KEY) && !isRTStarter){
                Device device = new Device(message.getSenderId());
                HashMap<String, Object> data = new HashMap<>();
                data.put(Constants.RT_TEST_REP_KEY, getAlphaNumericString(Integer.parseInt(MainActivity.this.editTextSize.getText().toString())));
                device.sendMessage(data);
            }

            updateLog(Constants.NORMAL, "Message Received: " + message.getSenderId() + ", content: " + message.getContent());
        }

        @Override
        public void onMessageFailed(Message message, MessageException e) {
            Log.e(TAG, "Message failed", e);
        }

        @Override
        public void onBroadcastMessageReceived(Message message) {

        }

        @Override
        public void onMessageSent(String messageId) {
            Log.e(TAG, "Message Sent");

        }
    };

    private void timerHello(final int time, Device device, boolean b1, Integer size) {

        timerHelloPackets = new Timer();

        timerHelloPackets.schedule(new TimerTask() {
            @Override
            public void run() {
                if (b1) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("text", getAlphaNumericString(size));
                    device.sendMessage(data);
                    timerHello(time, device, b1, size);
                }
            }
        }, time);
    }


    private void updateLog(int type, String msg) {
        TextView textView = findViewById(R.id.text_log);
        SpannableString contentText = new SpannableString(textView.getText());

        String htmlText = Html.toHtml(contentText);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        switch (type) {
            case Constants.NORMAL:
                textView.setText(Html.fromHtml("<font color='purple'>" +timestamp + " | " +  msg + "</font>" + htmlText ), TextView.BufferType.EDITABLE);
                break;
            case Constants.WARNING:
                textView.setText(Html.fromHtml( "<font color='blue'>" + timestamp + " | " + msg + "</font>" + htmlText), TextView.BufferType.EDITABLE);
                break;
            case Constants.ERROR:
            default:
                textView.setText(Html.fromHtml( "<font color='red'>" + timestamp + " | " +  msg + "</font>" + htmlText), TextView.BufferType.EDITABLE);
                break;
        }

    }
}