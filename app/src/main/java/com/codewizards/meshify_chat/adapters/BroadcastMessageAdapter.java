package com.codewizards.meshify_chat.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.models.Message;
import com.codewizards.meshify_chat.ui.chat.ChatActivity;
import com.codewizards.meshify_chat.util.Constants;
import com.codewizards.meshify_chat.util.MeshifyUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

        @Nullable
        @BindView(R.id.contactInitials)
        TextView mInitialsTextView;

        @Nullable
        @BindView(R.id.broadcast_user_name)
        TextView userName;

        @BindView(R.id.txtMessage)
        TextView txtMessage;

        @BindView(R.id.msgDate)
        TextView dateSentView;

        @Nullable
        @BindView(R.id.msgStatus)
        ImageView statusView;

        Message message;

        Context context;

        MessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.context = view.getContext();
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

            String[] stringArray = context.getResources().getStringArray(R.array.peer_actions);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(stringArray, (dialog, which) -> {
                switch (which) {
                    case 0: {
                        context.startActivity(new Intent(context.getApplicationContext(), ChatActivity.class)
                                .putExtra(Constants.INTENT_EXTRA_NAME,this.message.getUserName())
                                .putExtra(Constants.INTENT_EXTRA_LAST_SEEN, true)
                                .putExtra(Constants.INTENT_EXTRA_UUID,  this.message.getSenderId()));
                        break;
                    }
                    case 1:{
                        Toast.makeText(context, "Not Implemented", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            });

            AlertDialog create = builder.create();
            create.setCanceledOnTouchOutside(true);
            create.show();
        }
    }
}
