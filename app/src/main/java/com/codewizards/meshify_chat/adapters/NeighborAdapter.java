package com.codewizards.meshify_chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.ui.home.MainViewModel;
import com.codewizards.meshify_chat.util.MeshifyUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NeighborAdapter extends RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder> {

    private List<Neighbor> neighbors;
    private Context context;
    private MainViewModel mainViewModel;

    private OnItemClickListener listener;

    public NeighborAdapter(Context context) {
        this.neighbors = new ArrayList<>();
        this.context = context;

        mainViewModel =  new ViewModelProvider((ViewModelStoreOwner) context).get(MainViewModel.class);

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

    public int getNeighborPosition(String neighborId) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i).getUuid().equals(neighborId))
                return i;
        }
        return -1;
    }

    public Neighbor getNeighborAt(int position) {
        return neighbors.get(position);
    }

    public void setNeighbors(List<Neighbor> newData) {
        if (neighbors != null) {
            neighbors.clear();
            neighbors.addAll(newData);
            notifyDataSetChanged();
        } else {
            // first initialization
            neighbors = newData;
        }
    }

    public String getAllNeighbors() {
        return new Gson().toJson(this.neighbors);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Neighbor neighbor);
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder {
        final TextView mContentView;
//        final ImageView mImageView;
        final TextView mInitialsTextView;
        final ImageView mPopupMenu;
        final TextView mLastMsg;
        Neighbor neighbor;

        NeighborViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.neighborName);
//            mImageView = view.findViewById(R.id.neighborAvatar);
            mInitialsTextView = view.findViewById(R.id.contactInitials);
            mPopupMenu = view.findViewById(R.id.popupMenu);
            mLastMsg = view.findViewById(R.id.lastMessage);

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
                                    mainViewModel.insert(neighbor);
                                    Toast.makeText(context, "Saved " + neighbor.getDevice_name() + " to contacts", Toast.LENGTH_LONG).show();
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
                                case R.id.action_popup_remove:
                                    // call method to save
                                    mainViewModel.delete(neighbor);
                                    Toast.makeText(context,  "Removed " + neighbor.getDevice_name() + " from contacts", Toast.LENGTH_LONG).show();
                                    return true;
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
                    this.mContentView.setText(neighbor.getDevice_name());
                    this.mInitialsTextView.setText(MeshifyUtils.generateInitials(neighbor.getDevice_name()));
//                    ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.parseColor("#006257"));
                    break;
            }
            if (neighbor.isNearby()) {
                this.mContentView.setTextColor(Color.parseColor("#006257"));
                this.mLastMsg.setTextColor(Color.GREEN);
                this.mLastMsg.setText("Nearby");
//                this.mImageView.setImageResource(R.drawable.ic_user_green);
                ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.parseColor(MeshifyUtils.getRandomColor()));

            } else {
                this.mContentView.setTextColor(Color.GRAY);
                this.mLastMsg.setTextColor(Color.RED);
                this.mLastMsg.setText("Not in Range");
//                this.mImageView.setImageResource(R.drawable.ic_user_red);
                ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.RED);
            }
        }
    }

}
