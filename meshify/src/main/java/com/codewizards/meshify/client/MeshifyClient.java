package com.codewizards.meshify.client;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify.framework.controllers.MeshifyCore;
import com.codewizards.meshify.logs.Log;

import java.util.HashMap;

public class MeshifyClient {
    private static final String TAG = "[Meshify][MeshifyClient]" ;

    private String userUuid;

    private String bundleUuid;

    private String apiKey;

    private String publicKey;

    private String secretKey;

    private DeviceProfile deviceProfile;

    static class Builder {

        private SharedPreferences sharedPreferences;

        private SharedPreferences.Editor editor;

        private String userUuid;

        private String apiKey;

        private String packageName;

        private String publicKey;

        private String secretKey;

        private DeviceProfile deviceProfile;

        Builder(Context context) {
            if (context != null) {
                Context applicationContext = context.getApplicationContext();
                this.sharedPreferences = applicationContext.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);
                this.editor = sharedPreferences.edit();
                this.packageName = applicationContext.getPackageName();
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
            this.editor.putString("com.codewizards.meshify.key.public", this.publicKey);
            this.editor.putString("com.codewizards.meshify.key.secret", this.secretKey);
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setKeys() {
            this.publicKey = this.sharedPreferences.getString("com.codewizards.meshify.key.public", (String) null);
            this.secretKey = this.sharedPreferences.getString("com.codewizards.meshify.key.secret", (String) null);
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

    public String getBundleUuid() {
        return bundleUuid;
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

    private MeshifyClient(Builder builder) {
        this.userUuid = builder.userUuid;
        this.bundleUuid = builder.packageName;
        this.apiKey = builder.apiKey;
        this.publicKey = builder.publicKey;
        this.secretKey = builder.secretKey;
        this.deviceProfile = builder.deviceProfile;
    }

}
