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


import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "[Meshify][MainActivity]";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);


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
            Log.i(TAG, "Device Connected: " + device.getUserId() + " isClient: " + session.isClient());
            updateLog(Constants.NORMAL, "Device Connected: " + device.getUserId() + " | isClient: " + session.isClient()) ;
            timerHello(Constants.HELLO_PACKET_INTERVAL, device, session.isClient());
        }

        @Override
        public void onDeviceLost(Device device) {
            Log.w(TAG, "Device lost: " + device.getUserId());
            updateLog(Constants.ERROR, "Device lost: " + device.getUserId()) ;
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
            updateLog(Constants.NORMAL, "Message Received: " + message.getSenderId() + ", content: " + message.getContent()) ;
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
                    device.sendMessage(data);
                    Log.d(TAG, "Hello message sent!");
                    timerHello(time, device, b1);
                }
            }
        }, time);
    }


    private void updateLog(int type, String msg) {
        TextView textView = findViewById(R.id.text_log);
        ScrollView scrollView = findViewById(R.id.scrollViewText);
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