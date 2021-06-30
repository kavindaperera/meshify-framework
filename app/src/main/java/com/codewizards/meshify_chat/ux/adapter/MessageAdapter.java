package com.codewizards.meshify_chat.ux.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.entities.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View messageView = null;

        switch (viewType) {
            case Message.INCOMING_MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).inflate((R.layout.msg_row_incoming), parent, false);
                break;
            case Message.OUTGOING_MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).inflate((R.layout.msg_row_outgoing), parent, false);
                break;
        }

        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.setMessage(messages.get(position));
    }

    public void addMessage(Message message) {
        messages.add(0, message);
        notifyDataSetChanged();
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
        final TextView txtMessage;
        Message message;

        MessageViewHolder(View view) {
            super(view);
            txtMessage = view.findViewById(R.id.txtMessage);
        }

        void setMessage(Message message) {
            this.message = message;
            this.txtMessage.setText(message.getText());
        }
    }

}
