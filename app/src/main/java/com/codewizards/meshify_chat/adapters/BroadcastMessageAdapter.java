package com.codewizards.meshify_chat.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.models.Message;
import com.codewizards.meshify_chat.util.Constants;
import com.codewizards.meshify_chat.util.MeshifyUtils;

import java.util.List;

import butterknife.OnClick;
import butterknife.Optional;

public class BroadcastMessageAdapter extends RecyclerView.Adapter<BroadcastMessageAdapter.MessageViewHolder> {

    private final List<Message> messages;

    public BroadcastMessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public BroadcastMessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View messageView = null;

        switch (viewType) {
            case Message.INCOMING_BROADCAST__MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).inflate((R.layout.broadcast_message_row_inbound), parent, false);
                break;
            case Message.OUTGOING_BROADCAST_MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).inflate((R.layout.broadcast_message_row_outbound), parent, false);
                break;
        }

        return new BroadcastMessageAdapter.MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastMessageAdapter.MessageViewHolder holder, int position) {
        holder.setMessage(messages.get(position));
    }

    public void addMessage(Message message) {
        if (!isMessageExist(message)) {
            messages.add(0, message);
            notifyDataSetChanged();
        }
    }

    public boolean isMessageExist(Message message) {
        return messages.contains(message);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getDirection();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView mInitialsTextView;
        final TextView userName;
        final TextView txtMessage;
        final TextView dateSentView;
        final ImageView statusView;

        Message message;

        MessageViewHolder(View view) {
            super(view);
            mInitialsTextView = view.findViewById(R.id.contactInitials);
            userName = view.findViewById(R.id.broadcast_user_name);
            txtMessage = view.findViewById(R.id.txtMessage);
            dateSentView = view.findViewById(R.id.msgDate);
            statusView = view.findViewById(R.id.msgStatus);
        }

        void setMessage(Message message) {
            this.message = message;

            if (message.getDirection() == 2) {
                this.mInitialsTextView.setText(MeshifyUtils.generateInitials(message.getUserName()));
                ((GradientDrawable) this.mInitialsTextView.getBackground()).setColor(Color.parseColor(MeshifyUtils.getRandomColor()));
                this.userName.setText(message.getUserName());
            }

            this.txtMessage.setText(message.getMessage());

            if (this.dateSentView != null) {
                String valueOf = String.valueOf(System.currentTimeMillis());
                if (message.getDateSent() != null) {
                    valueOf = message.getDateSent();
                }
                String messageDate = MeshifyUtils.getMessageDate(valueOf);
                this.dateSentView.setText(messageDate);
            }
        }

        @OnClick({R.id.contactInitials, R.id.broadcast_user_name})
        @Optional
        public void showNeighborAction() {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USER_ID, this.message.getSenderId());
            bundle.putString(Constants.USER_NAME, this.message.getUserName());
        }
    }
}
