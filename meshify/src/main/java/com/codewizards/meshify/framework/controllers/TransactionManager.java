package com.codewizards.meshify.framework.controllers;

import android.os.AsyncTask;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
                transactionManager.start();
            }

        }
    }

    private static void start() {
        new AsyncTask(){
            protected synchronized Object doInBackground(Object[] params) {
                if (transactions.size() > 0) {
                    Transaction transaction = (Transaction) transactions.pollFirstEntry().getKey();
                    try {
                        transaction.getSession().flush(transaction.getMeshifyEntity());

                    }
                    catch (IOException iOException) {
                        Log.e(TAG, "IOException: ", iOException);
                        iOException.printStackTrace();

                    }
                    catch (MessageException messageException) {
                        Log.e(TAG, "MessageException: ", messageException);
                        messageException.printStackTrace();

                    }
                    catch (InterruptedException interruptedException) {
                        Log.e(TAG, "doInBackground: ", interruptedException);

                    }
                }
                return null;
            }
        }.execute(new Object[0]);
    }

}
