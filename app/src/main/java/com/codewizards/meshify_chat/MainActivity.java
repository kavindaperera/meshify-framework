package com.codewizards.meshify_chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.StateListener;

public class MainActivity extends AppCompatActivity {

    private String TAG = "[Meshify][MainActivity]";
    static final String PAYLOAD_DEVICE_TYPE  = "device_type";
    static final String PAYLOAD_DEVICE_NAME  = "device_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Configure the Toolbar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        Meshify.debug = BuildConfig.DEBUG;

        Meshify.initialize(getApplicationContext());

        startMeshify();

    }

    /**
     * start meshify
     */
    private void startMeshify() {

        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);

        Meshify.start(null, stateListener, builder.build());

    }


    StateListener stateListener = new StateListener(){

        @Override
        public void onStarted() {
            super.onStarted();
            Log.e(TAG, "onStarted()");
        }

        @Override
        public void onStartError(String message, int errorCode) {
            super.onStartError(message, errorCode);
            Log.e(TAG, "onStartError() " + message);
            if (errorCode == stateListener.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMeshify(); //start meshify again after permission is granted
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show(); //close app on deny permissions
            finish();
        }

    }
}