package com.codewizards.meshify_chat.ux.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.client.StateListener;
import com.codewizards.meshify_chat.BuildConfig;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.entities.Neighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static String TAG = "[Meshify][MainActivity]";
    static final String PAYLOAD_DEVICE_TYPE  = "device_type";
    static final String PAYLOAD_DEVICE_NAME  = "device_name";

    NeighborsRecyclerViewAdapter neighborAdapter = new NeighborsRecyclerViewAdapter(new ArrayList<>());

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Configure the Toolbar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        RecyclerView recyclerView = findViewById(R.id.neighbor_list);
        recyclerView.setAdapter(neighborAdapter);

        progressDialog = new ProgressDialog(MainActivity.this);

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

        Meshify.start(messageListener, stateListener, builder.build());

    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            super.onMessageReceived(message);
            Log.e(TAG, "onMessageReceived()" + message);
        }
    };

    StateListener stateListener = new StateListener(){

        @Override
        public void onStarted() {
            super.onStarted();
            Log.e(TAG, "onStarted()");

            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }

        @Override
        public void onStartError(String message, int errorCode) {
            super.onStartError(message, errorCode);
            Log.e(TAG, "onStartError() " + message);

            progressDialog.dismiss();

            if (errorCode == stateListener.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
            }
        }

        @Override
        public void onDeviceConnected(Device device, Session session) {
            super.onDeviceConnected(device, session);
            Log.d(TAG, "onDeviceConnected() " + device + " session: " + session);

            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName());
            neighbor.setNearby(true);
            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
            neighborAdapter.addNeighbor(neighbor);

            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), "Neighbor added: " + neighbor.getDeviceName(), Toast.LENGTH_SHORT).show();

            // send our details to the Device
            HashMap<String, Object> map = new HashMap<>();
            map.put(PAYLOAD_DEVICE_NAME, Build.MANUFACTURER + " " + Build.MODEL);
            map.put(PAYLOAD_DEVICE_TYPE, Neighbor.DeviceType.ANDROID.ordinal());
            device.sendMessage(map);

        }

        @Override
        public void onDeviceBlackListed(Device device) {
            super.onDeviceBlackListed(device);
            Log.e(TAG, "onDeviceBlackListed() " + device);
            neighborAdapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "device " + device.getDeviceName() + " got black listed", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            super.onDeviceLost(device);
            Log.e(TAG, "onDeviceLost() " + device);
            neighborAdapter.removeNeighbor(device);
            Toast.makeText(getApplicationContext(), "Lost Device " + device.getDeviceName(), Toast.LENGTH_LONG).show();

        }
    };


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

/**
 * Neighbors Recycler View
 */

class NeighborsRecyclerViewAdapter extends RecyclerView.Adapter<NeighborsRecyclerViewAdapter.NeighborViewHolder> {

    private final List<Neighbor> neighbors;

    NeighborsRecyclerViewAdapter(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    void addNeighbor(Neighbor neighbor) {
        int position = getNeighborPosition(neighbor.getUuid());

        if (position > -1) {
            neighbors.set(position, neighbor);
            notifyItemChanged(position);
        } else {
            neighbors.add(neighbor);
            notifyItemInserted(neighbors.size() - 1);
        }
    }

    void removeNeighbor(Device lostNeighbor) {
        int position = getNeighborPosition(lostNeighbor.getUserId());

        if (position > -1) {
            Neighbor neighbor = neighbors.get(position);
            neighbor.setNearby(false);
            neighbors.set(position, neighbor);
            notifyItemChanged(position);
        }
    }

    private int getNeighborPosition(String neighborId) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i).getUuid().equals(neighborId))
                return i;
        }
        return -1;
    }

    @Override
    public NeighborViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.neighbor_row, parent, false);
        return new NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NeighborViewHolder neighborHolder, int position) {
        neighborHolder.setNeighbor(neighbors.get(position));
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView mContentView;
        final ImageView mImageView;
        Neighbor neighbor;

        NeighborViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.neighborName);
            mImageView  = view.findViewById(R.id.neighborAvatar);
            view.setOnClickListener(this);
        }

        void setNeighbor(Neighbor neighbor) {
            this.neighbor = neighbor;

            switch (neighbor.getDeviceType()) {
                case ANDROID:
                    this.mContentView.setText(neighbor.getDeviceName() + " (android)");
                    break;
            }

            if (neighbor.isNearby()) {
                this.mContentView.setTextColor(Color.parseColor("#006257"));
                this.mImageView.setImageResource(R.drawable.ic_user_green);
            } else {
                this.mContentView.setTextColor(Color.GRAY);
                this.mImageView.setImageResource(R.drawable.ic_user_red);
            }
        }

        public void onClick(View v) {

        }
    }

}