package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import com.codewizards.meshify.framework.controllers.transactionmanager.Transaction;

import java.util.ArrayList;

public class GattTransaction {

    final ArrayList<GattOperation> gattOperations = new ArrayList<>();

    Transaction transaction;

    GattTransaction(Transaction transaction1) {
        this.transaction = transaction1;
    }

    void addGattOperation(GattOperation gattOperation) {
        this.gattOperations.add(gattOperation);

    }

    public ArrayList<GattOperation> getGattOperations() {
        return this.gattOperations;
    }

    Transaction getTransaction() {
        return this.transaction;
    }

}
