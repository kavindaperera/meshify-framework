package com.codewizards.meshify_chat.ui.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "[Meshify][ChatViewModel]" ;

    public ChatViewModel(@NonNull Application application) {
        super(application);
    }
}
