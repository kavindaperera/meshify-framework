package com.codewizards.meshify.framework.controllers;

import android.os.AsyncTask;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
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

    private static void startInBackground() {

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (transactions.size() > 0) {
                    Transaction transaction = (Transaction) transactions.pollFirstEntry().getKey();
                    try {
                        transaction.getSession().flush(transaction.getMeshifyEntity());

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

}
