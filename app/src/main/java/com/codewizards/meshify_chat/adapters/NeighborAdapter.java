package com.codewizards.meshify_chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.logs.Log;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.models.Neighbor;

import java.util.List;

public class NeighborAdapter extends RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder> {

    private final List<Neighbor> neighbors;
    private Context context;

    private OnItemClickListener listener;

    public NeighborAdapter(Context context, List<Neighbor> neighbors) {
        this.neighbors = neighbors;
        this.context = context;
    }

    @NonNull
    @Override
    public NeighborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.neighbor_row, parent, false);

        return new NeighborAdapter.NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NeighborViewHolder holder, int position) {
        holder.setNeighbor(neighbors.get(position));
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    public void addNeighbor(Neighbor neighbor) {
        int position = getNeighborPosition(neighbor.getUuid());

        if (position > -1) {
            neighbors.set(position, neighbor);
            notifyItemChanged(position);
        } else {
            neighbors.add(neighbor);
            notifyItemInserted(neighbors.size() - 1);
        }
    }

    public void updateNeighbor(String senderId, String userName) {
        int position = getNeighborPosition(senderId);
        if (position > -1) {
            Neighbor neighbor = neighbors.get(position);
            neighbor.setDeviceName(userName);
            notifyItemChanged(position);
        }

    }

    public void removeNeighbor(Device lostNeighbor) {
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

    public Neighbor getNeighborAt(int position) {
        return neighbors.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Neighbor neighbor);
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder {
        final TextView mContentView;
        final ImageView mImageView;
        final ImageView mPopupMenu;
        Neighbor neighbor;

        NeighborViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.neighborName);
            mImageView = view.findViewById(R.id.neighborAvatar);
            mPopupMenu = view.findViewById(R.id.popupMenu);

            mPopupMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(),v);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.action_popup_save:
                                    // call method to save
                                    return true;
                                case R.id.action_popup_disconnect:{
                                    Device device = neighbor.getDevice();
                                    if (device!=null) {
                                        Meshify.getInstance().getMeshifyCore().disconnectDevice(device);
                                    }
                                    return true;
                                }
                                case R.id.action_popup_connect:{
                                    Device device = neighbor.getDevice();
                                    if (device!=null) {
                                        Meshify.getInstance().getMeshifyCore().connectDevice(device);
                                    }
                                    return true;
                                }
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(neighbors.get(position));
                    }
                }
            });
        }

        void setNeighbor(Neighbor neighbor) {
            this.neighbor = neighbor;
            switch (neighbor.getDeviceType()) {
                case ANDROID:
                    this.mContentView.setText(neighbor.getDeviceName());
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
    }

}
