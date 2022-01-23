package com.codewizards.meshify.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify.api.profile.DeviceProfile;
import com.codewizards.meshify.framework.controllers.MeshifyCore;
import com.codewizards.meshify.logs.Log;

import java.util.HashMap;

public class MeshifyClient {
    private static final String TAG = "[Meshify][MeshifyClient]" ;

    private String userUuid;

    private String apiKey;

    private String publicKey;

    private String secretKey;

    private String phone;

    private DeviceProfile deviceProfile;

    private long ownSequenceNum;

    static class Builder {

        private SharedPreferences sharedPreferences;

        private SharedPreferences.Editor editor;

        private String userUuid;

        private String apiKey;

        private String publicKey;

        private String secretKey;

        private String phone = null;

        private DeviceProfile deviceProfile;

        private long ownSequenceNum;

        Builder(Context context) {
            if (context != null) {
                Context applicationContext = context.getApplicationContext();
                this.sharedPreferences = applicationContext.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);
                this.editor = sharedPreferences.edit();
                this.ownSequenceNum = Constants.FIRST_SEQUENCE_NUMBER;
                this.deviceProfile = new DeviceProfile(applicationContext);
                return;
            }
            throw new IllegalArgumentException("Context can not be null.");
        }

        public Builder setUserUuid(String userUuid) {
            this.userUuid = userUuid;
            return this;
        }

        public Builder generateKeyPair() throws Exception {
            HashMap<String, String> hashMap = MeshifyRSA.generateKeyPair();
            this.publicKey = hashMap.get("public");
            this.secretKey = hashMap.get("secret");
            Log.d(TAG, "... generating new key pair");
            this.editor.putString(MeshifyCore.PREFS_PUBLIC_KEY, this.publicKey);
            this.editor.putString(MeshifyCore.PREFS_PRIVATE_KEY, this.secretKey);
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setKeys() {
            this.publicKey = this.sharedPreferences.getString(MeshifyCore.PREFS_PUBLIC_KEY, (String) null);
            this.secretKey = this.sharedPreferences.getString(MeshifyCore.PREFS_PRIVATE_KEY, (String) null);
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public MeshifyClient build() {
            this.editor.putString(MeshifyCore.PREFS_USER_UUID, this.userUuid);
            this.editor.apply();
            return new MeshifyClient(this);
        }
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getUserUuid() {
        return this.userUuid;
    }

    public String getPhone() {
        return this.phone;
    }

    /**
     * Method allowing to increment the sequence number
     */
    public long getNextSequenceNumber() {
        if (ownSequenceNum < Constants.MAX_VALID_SEQ_NUM) {
            ++ownSequenceNum;
        } else {
            ownSequenceNum = Constants.MIN_VALID_SEQ_NUM;
        }
        return ownSequenceNum;
    }


    private MeshifyClient(Builder builder) {
        this.userUuid = builder.userUuid;
        this.apiKey = builder.apiKey;
        this.publicKey = builder.publicKey;
        this.secretKey = builder.secretKey;
        this.phone = builder.phone;
        this.deviceProfile = builder.deviceProfile;
    }

}
