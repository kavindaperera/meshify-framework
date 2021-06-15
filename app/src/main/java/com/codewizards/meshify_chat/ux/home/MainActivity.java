package com.codewizards.meshify_chat.ux.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.codewizards.meshify.client.Session;
import com.codewizards.meshify.client.StateListener;
import com.codewizards.meshify_chat.BuildConfig;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.entities.Neighbor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static String TAG = "[Meshify][MainActivity]";
    static final String PAYLOAD_DEVICE_TYPE  = "device_type";
    static final String PAYLOAD_DEVICE_NAME  = "device_name";

    NeighborsRecyclerViewAdapter peersAdapter = new NeighborsRecyclerViewAdapter(new ArrayList<>());

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
        recyclerView.setAdapter(peersAdapter);

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

        Meshify.start(null, stateListener, builder.build());

    }


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
            Log.d(TAG, "onDeviceConnected() " + device);

            Neighbor neighbor = new Neighbor(device.getUserId(), device.getDeviceName());
            neighbor.setNearby(true);
            neighbor.setDeviceType(Neighbor.DeviceType.ANDROID);
            peersAdapter.addPeer(neighbor);

            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), "Neighbor added: " + neighbor.getDeviceName(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Neighbor added: " + neighbor.getDeviceName());
        }

        @Override
        public void onDeviceBlackListed(Device device) {
            super.onDeviceBlackListed(device);
            Log.e(TAG, "onDeviceBlackListed() " + device);
            peersAdapter.removePeer(device);
            Toast.makeText(getApplicationContext(), "device " + device.getDeviceName() + " got black listed", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onDeviceLost(Device device) {
            super.onDeviceLost(device);
            Log.e(TAG, "onDeviceLost() " + device);
            peersAdapter.removePeer(device);
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
 * Recycler View
 */

class NeighborsRecyclerViewAdapter extends RecyclerView.Adapter<NeighborsRecyclerViewAdapter.PeerViewHolder> {

    private final List<Neighbor> neighbors;

    NeighborsRecyclerViewAdapter(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    void addPeer(Neighbor neighbor) {
        int position = getPeerPosition(neighbor.getUuid());

        if (position > -1) {
            neighbors.set(position, neighbor);
            notifyItemChanged(position);
        } else {
            neighbors.add(neighbor);
            notifyItemInserted(neighbors.size() - 1);
        }
    }

    void removePeer(Device lostPeer) {
        int position = getPeerPosition(lostPeer.getUserId());

        if (position > -1) {
            Neighbor peer = neighbors.get(position);
            peer.setNearby(false);
            neighbors.set(position, peer);
            notifyItemChanged(position);
        }
    }

    private int getPeerPosition(String peerId) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i).getUuid().equals(peerId))
                return i;
        }
        return -1;
    }

    @Override
    public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.neighbor_row, parent, false);
        return new PeerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PeerViewHolder peerHolder, int position) {
        peerHolder.setPeer(neighbors.get(position));
    }

    class PeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView mContentView;
        Neighbor neighbor;

        PeerViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.neighborName);
            view.setOnClickListener(this);
        }

        void setPeer(Neighbor peer) {
            this.neighbor = peer;

            switch (peer.getDeviceType()) {
                case ANDROID:
                    this.mContentView.setText(peer.getDeviceName() + " (android)");
                    break;
            }

            if (peer.isNearby()) {
                this.mContentView.setTextColor(Color.parseColor("#006257"));
            } else {
                this.mContentView.setTextColor(Color.RED);
            }
        }

        public void onClick(View v) {

        }
    }

}