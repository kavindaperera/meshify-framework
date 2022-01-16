package com.codewizards.meshify_chat.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.ui.intro.IntroActivity;
import com.codewizards.meshify_chat.ui.signup.SignupActivity;
import com.codewizards.meshify_chat.util.Constants;

public class SettingsFragment extends PreferenceFragmentCompat {

    Preference verification;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(Constants.PREFS_NAME);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (MeshifySession.isVerified()) {
            Preference preference = findPreference("verification");
            this.verification = preference;
            preference.setSummary("Verified");
            preference.setOnPreferenceClickListener(preference1 -> {
                Toast.makeText(getContext(), "Phone Number Already Verified", Toast.LENGTH_SHORT).show();
                return false;
            });
        }
    }
}