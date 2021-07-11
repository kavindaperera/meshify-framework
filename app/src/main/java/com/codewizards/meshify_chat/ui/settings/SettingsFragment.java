package com.codewizards.meshify_chat.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.utils.Constants;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(Constants.PREFS_NAME);

        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}