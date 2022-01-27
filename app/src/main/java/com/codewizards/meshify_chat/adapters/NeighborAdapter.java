package com.codewizards.meshify_chat.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify.api.Device;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.util.MeshifyUtils;


public class NeighborAdapter extends ListAdapter<Neighbor, NeighborAdapter.NeighborViewHolder> {
    private OnItemClickListener listener;

    public NeighborAdapter() {
        super(DIFF_CALLBACK);

    }

    private static final DiffUtil.ItemCallback<Neighbor> DIFF_CALLBACK = new DiffUtil.ItemCallback<Neighbor>() {
        @Override
        public boolean areItemsTheSame(@NonNull Neighbor oldItem, @NonNull Neighbor newItem) {
            return oldItem.getUuid().equals(newItem.getUuid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Neighbor oldItem, @NonNull Neighbor newItem) {
            return oldItem.getDeviceName().equals(newItem.getDeviceName()) &&
                    oldItem.isNearby() == newItem.isNearby() && oldItem.getLastSeen().equals(newItem.getLastSeen());
        }
    };


    @NonNull
    @Override
    public NeighborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.neighbor_row, parent, false);

        return new NeighborAdapter.NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NeighborViewHolder holder, int position) {
        holder.setNeighbor(getItem(position));
    }


    public Neighbor getNeighborAt(int position) {
        return getItem(position);
    }

    public Neighbor getNeighborById(String id) {
        for (int i = 0; i < getItemCount(); i++){
            Neighbor n1 = getItem(i);
            if (n1.getUuid().equals(id)){
                return n1;
            }
        }
        return null;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Neighbor neighbor);
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder {
        final TextView mContentView;
        final TextView mInitialsTextView;
        final ImageView mPopupMenu;
        final TextView mLastMsg;
        final TextView mLastSeen;
        Neighbor neighbor;

        NeighborViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.neighborName);
            mInitialsTextView = view.findViewById(R.id.contactInitials);
            mPopupMenu = view.findViewById(R.id.popupMenu);
            mLastMsg = view.findViewById(R.id.lastMessage);
            mLastSeen =view.findViewById(R.id.lastSeen);

            mPopupMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(),v);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
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
                                        try {
                                            Meshify.getInstance().getMeshifyCore().connectDevice(device);
                                        } catch (Exception e) {

                                        }
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
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }

        void setNeighbor(Neighbor neighbor) {
            this.neighbor = neighbor;
            switch (neighbor.getDeviceType()) {
                case ANDROID:
                    this.mContentView.setText(neighbor.getDeviceName());
                    this.mInitialsTextView.setText(MeshifyUtils.generateInitials(neighbor.getDeviceName()));
                    this.mLastSeen.setText(MeshifyUtils.getMessageDate(neighbor.getLastSeen()));
                    break;
            }
            if (neighbor.isNearby()) {
                this.mContentView.setTextColor(Color.parseColor("#006257"));
                this.mLastMsg.setTextColor(Color.GREEN);
                this.mLastMsg.setText("Nearby");
                ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.parseColor(MeshifyUtils.getRandomColor()));

            } else {
                this.mContentView.setTextColor(Color.GRAY);
                this.mLastMsg.setTextColor(Color.RED);
                this.mLastMsg.setText("Not in Range");
                ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.RED);
            }
        }
    }

}
