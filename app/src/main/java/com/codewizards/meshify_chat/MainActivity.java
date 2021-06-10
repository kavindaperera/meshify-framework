package com.codewizards.meshify_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.logs.Log;

public class MainActivity extends AppCompatActivity {

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

        Config.Builder builder=new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH);

        //TODO - Meshify.start()

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMeshify();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
}