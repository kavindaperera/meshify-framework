package com.codewizards.meshify.logs;

import com.codewizards.meshify.logs.logentities.LogEntity;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class AsyncLogHandler implements Runnable {

    private static String TAG = "[Meshify][AsyncLogHandler]";

    private BlockingQueue<LogEntity> blockingQueue = new LinkedBlockingDeque<LogEntity>();

    AsyncLogHandler(BlockingQueue<LogEntity> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {

        try {
            while (true) {
                LogEntity logEntity = this.blockingQueue.take();
                if (MeshifyLogger.getInstance().shouldWriteToTempFile()) {
                    this.writeLog(logEntity);
                }
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            return;
        }
    }

    private void writeLog(LogEntity logEntity) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(MeshifyLogger.getOrCreateFile(MeshifyLogger.LOG_FILE), true);
            fileOutputStream.write(new Gson().toJson((Object) logEntity).getBytes());
            fileOutputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
