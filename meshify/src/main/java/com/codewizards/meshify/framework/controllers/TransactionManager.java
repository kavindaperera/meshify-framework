package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;

public class TransactionManager {
    private static String TAG = "[Meshify][TransactionManager]";

    private static TransactionManager transactionManager = new TransactionManager();

    private static ConcurrentNavigableMap<Transaction, Boolean> transactions = new ConcurrentSkipListMap<Transaction, Boolean>();

    private TransactionManager() {
    }

    static void sendEntity(Session session, MeshifyEntity meshifyEntity) {
        Session session2 = session;
        synchronized (session2) {
            Transaction transaction = new Transaction(session, meshifyEntity, transactionManager);
            if (session.getAntennaType() != Config.Antenna.BLUETOOTH_LE) {
                transactions.put(transaction, Boolean.TRUE);
                transactionManager.startInBackground();
            }

        }
    }

    private static void startInBackground() { //for bluetooth

        Executor executor = command -> command.run();

        executor.execute((Runnable) () -> {
            if (transactions.size() > 0) {
                Transaction transaction = (Transaction) transactions.pollFirstEntry().getKey();
                try {
                    transaction.getSession().flush(transaction.getMeshifyEntity());
                    transaction.getTransactionManager().notifySent(transaction);
                }
                catch (IOException iOException) {
                    Log.e(TAG, "startInBackground:IOException ", iOException);
                    iOException.printStackTrace();

                }
                catch (MessageException messageException) {
                    Log.e(TAG, "startInBackground:MessageException ", messageException);
                    messageException.printStackTrace();

                }
                catch (InterruptedException interruptedException) {
                    Log.e(TAG, "startInBackground:InterruptedException ", interruptedException);

                }
            }
        });

//        new AsyncTask(){
//            protected synchronized Object doInBackground(Object[] params) {
//                if (transactions.size() > 0) {
//                    Transaction transaction = (Transaction) transactions.pollFirstEntry().getKey();
//                    try {
//                        transaction.getSession().flush(transaction.getMeshifyEntity());
//
//                    }
//                    catch (IOException iOException) {
//                        Log.e(TAG, "doInBackground:IOException ", iOException);
//                        iOException.printStackTrace();
//
//                    }
//                    catch (MessageException messageException) {
//                        Log.e(TAG, "doInBackground:MessageException ", messageException);
//                        messageException.printStackTrace();
//
//                    }
//                    catch (InterruptedException interruptedException) {
//                        Log.e(TAG, "doInBackground:InterruptedException ", interruptedException);
//
//                    }
//                }
//                return null;
//            }
//        }.execute(new Object[0]);
    }

    private void notifySent(Transaction transaction) {
        switch (transaction.getMeshifyEntity().getEntity()) {
            case 1: {
                List<Message> list = this.getMessageListFromTransaction(transaction);
                for (Message message : list) {
                    if (Meshify.getInstance().getMeshifyCore().getMessageListener() == null) continue;
                    new Handler(Looper.getMainLooper()).post(() -> {
                       Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageSent(message.getUuid());
                    });
                }
                break;
            }
        }
    }
    private List<Message> getMessageListFromTransaction(Transaction tr1) {
        ArrayList<Message> arrayList = new ArrayList<Message>();
        switch (tr1.getMeshifyEntity().getEntity()) {
            case 1: {
                MeshifyContent meshifyContent = (MeshifyContent) tr1.getMeshifyEntity().getContent();
                Message.Builder builder = new Message.Builder();
                builder.setContent(meshifyContent.getPayload());
                builder.setReceiverId(null);
                Message message = builder.build();
                message.setUuid(meshifyContent.getId());
                arrayList.add(message);
            }
        }
        return arrayList;
    }

}
