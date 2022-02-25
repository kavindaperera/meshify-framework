package com.codewizards.meshify.framework.controllers.transactionmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.GattTransaction;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations.GattDataService;
import com.codewizards.meshify.framework.controllers.discoverymanager.BluetoothController;
import com.codewizards.meshify.framework.controllers.helper.BluetoothUtils;
import com.codewizards.meshify.framework.controllers.sessionmanager.Session;
import com.codewizards.meshify.framework.controllers.sessionmanager.SessionManager;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;

public class TransactionManager {
    private static String TAG = "[Meshify][TransactionManager]";

    private static TransactionManager transactionManager = new TransactionManager();
    private static ConcurrentSkipListMap<Session, ConcurrentNavigableMap<Transaction, Boolean>> bleTransactionsQueue = new ConcurrentSkipListMap();
    private static ConcurrentNavigableMap<Transaction, Boolean> bleTransactions = new ConcurrentSkipListMap<Transaction, Boolean>();
    private static ConcurrentNavigableMap<Transaction, Boolean> transactions = new ConcurrentSkipListMap<Transaction, Boolean>();

    private TransactionManager() {
    }

    public static void sendEntity(Session session, MeshifyEntity meshifyEntity) {
        synchronized (session) {
            Transaction transaction = new Transaction(session, meshifyEntity, transactionManager);
            if (session.getAntennaType() != Config.Antenna.BLUETOOTH_LE) {
                transactions.put(transaction, Boolean.TRUE);
                startInBackground();
            } else if (session.checkGatt()) {
                Log.i(TAG, "sendEntity:");
                if (getTransactionQueue(session).containsKey(transaction)){
                    Log.i(TAG, "sendEntity: transaction already queued");
                } else {
                    Log.i(TAG, "sendEntity: transaction was queued");
                    getTransactionQueue(session).put(transaction, Boolean.TRUE);
                }
                executeWithCharacteristicChange(session);
            } else {
                bleTransactions.put(transaction, Boolean.TRUE);
                start();
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

    public void onTransactionFinished(Transaction transaction) {
        Session session = transaction.getSession();
        Log.e(TAG, "onTransactionFinished: \n" + session.getDevice().getDeviceName() + "\n id " + session.getUserId() + "\n central: " + session.getUserId());
        this.notifySent(transaction);
        // TODO
    }

    private static void start() { // for BLE
        Transaction transaction2 = bleTransactions.pollFirstEntry().getKey();
        Session session = transaction2.getSession();
        GattTransaction gattTransaction = new GattTransaction(transaction2);
        for (int idx = 0; idx < transaction2.getByteArr().size(); ++idx) {
            GattDataService gattData = new GattDataService(session.getBluetoothGatt().getDevice(),
                    BluetoothUtils.getBluetoothUuid(),
                    BluetoothUtils.getCharacteristicUuid(),
                    transaction2.getByteArr().get(idx));
            gattData.setTransaction(transaction2);
            gattTransaction.addGattOperation(gattData);
        }
        BluetoothController.getGattManager().addAndStart(gattTransaction);
    }

    private static ConcurrentNavigableMap getTransactionQueue(Session session) {
        ConcurrentNavigableMap<Transaction, Boolean> concurrentNavigableMap = bleTransactionsQueue.get(session);
        if (concurrentNavigableMap == null) {
            concurrentNavigableMap = new ConcurrentSkipListMap<Transaction, Boolean>();
            bleTransactionsQueue.put(session, concurrentNavigableMap);
        }
        return concurrentNavigableMap;
    }

    private static synchronized void executeWithCharacteristicChange(Session session) {
        if (session.checkGatt() && bleTransactionsQueue.get(session).size() > 0) {
            TransactionManager.changeCharacteristic(session);
        } else {
            for (Session session2 : bleTransactionsQueue.keySet()) {
                if (session2.equals(session) || bleTransactionsQueue.get(session2).size() <= 0) continue;
                TransactionManager.changeCharacteristic(session2);
                return;
            }
        }
    }

    private static void changeCharacteristic(Session session) {
        try {
            BluetoothGattCharacteristic characteristic = session.getGattServer().getService(BluetoothUtils.getBluetoothUuid()).getCharacteristic(BluetoothUtils.getCharacteristicUuid());
            characteristic.setValue(new byte[]{1});
            session.getGattServer().notifyCharacteristicChanged(session.getBluetoothDevice(), characteristic, false);
        } catch (NullPointerException e) {
            SessionManager.disconnectLeDevice(session.getSessionId());
        }
    }

    public static Transaction getTransactionFromDevice(BluetoothDevice bluetoothDevice) {
        for (ConcurrentNavigableMap<Transaction, Boolean> keySet : bleTransactionsQueue.values()) {

            for (Transaction transaction2 : keySet.keySet()) {
                if (!transaction2.getBluetoothDevice().equals((Object)bluetoothDevice)) continue;
                return transaction2;
            }
        }
        return null;
    }

}
