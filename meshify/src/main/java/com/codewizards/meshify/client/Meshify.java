package com.codewizards.meshify.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codewizards.meshify.BuildConfig;
import com.codewizards.meshify.framework.controllers.MeshifyCore;
import com.codewizards.meshify.logs.Log;

import java.util.UUID;

public class Meshify {
    private static final String TAG = "[Meshify][Meshify]" ;

    public static boolean debug = false;

    private MeshifyClient meshifyClient;

    static Meshify meshifyInstance;

    private MeshifyCore meshifyCore;

    private Context context;

    private Config config;

    public static Meshify getInstance() {
        if (meshifyInstance != null) {
            return meshifyInstance;
        }
        throw new IllegalStateException("Meshify must be initialized before trying to reference it.");
    }

    private Meshify (Builder builder) {
        this.context = builder.context;
        this.meshifyClient = builder.meshifyClient;
    }

    public static void initialize (Context context) {
        Meshify.initialize(context, (String) null,  (UUID) null);
    }

    public static void initialize (Context context, String apiKey) {
        Meshify.initialize(context, apiKey,(UUID) null);
    }

    private static void initialize(Context context, @Nullable String providedApiKey, UUID uuid)  {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);
        String loadApiKey = Meshify.loadApiKey(context, providedApiKey);
        String string = sharedPreferences.getString(MeshifyCore.PREFS_USER_UUID, (String) null);

        if ((uuid == null || uuid.toString().equals(string)) && string != null) {
            Meshify.create(context, new MeshifyClient.Builder(context).setApiKey(loadApiKey).setUserUuid(string).setKeys().build());
            return;
        }

        try {
            String uuid2 = uuid == null ? UUID.randomUUID().toString() : uuid.toString();
            MeshifyClient meshifyClient = new MeshifyClient.Builder(context).setApiKey(loadApiKey).setUserUuid(uuid2).generateKeyPair().build();
            Meshify.create(context, meshifyClient);
        } catch (Exception exception) {
            exception.printStackTrace();
        }


    }

    public static void create(Context context, MeshifyClient meshifyClient) {
        synchronized (Meshify.class) {
            createInstance(new Builder(context, meshifyClient).build());
        }
    }

    private static String loadApiKey(Context context,@Nullable String providedApiKey) throws IllegalArgumentException {
        block4: {
            SharedPreferences sharedPreferences = context.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME, 0);
            if (providedApiKey == null) {
                try {
                    providedApiKey = (String) context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.get(MeshifyCore.PREFS_API_KEY);

                    if (providedApiKey == null && (providedApiKey = sharedPreferences.getString(MeshifyCore.PREFS_API_KEY, (String) null)) == null) {
                        throw new IllegalArgumentException("Missing UUID KEY ");
                    }
                    break block4;
                }
                catch (Exception exception) {
                    throw new IllegalArgumentException("Missing or incorrect UUID KEY");
                }
            }
            sharedPreferences.edit().putString(MeshifyCore.PREFS_API_KEY, providedApiKey).apply();
        }
        return providedApiKey;
    }

    public static void start(@Nullable MessageListener messageListener, @Nullable StateListener stateListener) {
        Meshify.start(messageListener, stateListener, new Config.Builder().build());
    }

    public static void start(@Nullable MessageListener messageListener, @Nullable StateListener stateListener, Config config) {

        try {
            if (getInstance().getMeshifyCore() == null) {
                MeshifyUtils.initialize(getInstance().getContext(), config);
                getInstance().setConfig(config);
                getInstance().setMeshifyCore(new MeshifyCore(getInstance().getContext(), config));
                getInstance().getMeshifyCore().setMessageListener(messageListener);
                getInstance().getMeshifyCore().setStateListener(stateListener);
                getInstance().getMeshifyCore().initializeServices();

                if (stateListener != null) {
                    stateListener.onStarted();
                }

            }
        } catch (NullPointerException nullPointerException) {
            if (stateListener != null) {
                stateListener.onStartError(stateListener.INITIALIZATION_ERROR_STRING, stateListener.INITIALIZATION_ERROR);
            } else {
                nullPointerException.printStackTrace();
            }
        }  catch (MeshifyException meshifyException) {
            if (stateListener != null) {
                stateListener.onStartError(meshifyException.getMessage(), meshifyException.getErrorCode());
            } else {
                meshifyException.printStackTrace();
            }
        }

    }

    public static boolean stop() {
        return false;
    }

    public static boolean pause() {
        return false;
    }

    public static boolean resume() {
        return false;
    }

    public static String sendMessage(@NonNull Message message) {
        try {
            return Meshify.sendMessage(message, getInstance().getConfig().getConfigProfile());
        } catch (NullPointerException e) {
            Log.e(TAG, "Meshify must be started with Meshify.start() to send a message");
            return null;
        }
    }

    public static String sendMessage(@NonNull Message message, ConfigProfile configProfile) {
        Meshify.isMessageNull(message);
        try {
            message.setSenderId(getInstance().getMeshifyClient().getUserUuid());
            getInstance().getMeshifyCore().sendMessage(message, message.getReceiverId(), configProfile);
            return message.getUuid();
        } catch (NullPointerException e) {
            Log.e(TAG, "Meshify must be started with Meshify.start() to send a message" );
            e.printStackTrace();
            return null;
        }
    }


    public Config getConfig() {
        return this.config;
    }

    public MeshifyClient getMeshifyClient() {
        return this.meshifyClient;
    }

    public MeshifyCore getMeshifyCore() {
        return this.meshifyCore;
    }

    public void setMeshifyCore(MeshifyCore meshifyCore) {
        this.meshifyCore = meshifyCore;
    }

    private static void createInstance(Meshify meshify) {
        meshifyInstance = meshify;
    }

    public Context getContext() {
        return this.context;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    private static void isMessageNull(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
    }

    private static class Builder {

        private final Context context;

        private final MeshifyClient meshifyClient;

        Builder(Context context, MeshifyClient meshifyClient) {

            if (context == null | meshifyClient == null) {
                throw new IllegalArgumentException("Context or Client is null.");
            }

            this.context = context.getApplicationContext();
            this.meshifyClient = meshifyClient;

        }

        public Meshify build() {
            return new Meshify(this);
        }

    }

}
