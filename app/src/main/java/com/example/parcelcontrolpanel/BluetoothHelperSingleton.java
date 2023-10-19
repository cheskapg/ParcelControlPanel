package com.example.parcelcontrolpanel;

import android.content.Context;

public class BluetoothHelperSingleton {
    private static BluetoothHelper instance;

    public static BluetoothHelper getInstance(Context context, String deviceName, String deviceAddress) {
        if (instance == null) {
            instance = new BluetoothHelper(context, deviceName, deviceAddress);
        }
        return instance;
    }
}
