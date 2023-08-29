package com.example.parcelcontrolpanel;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.Set;
import java.util.UUID;

public class BluetoothGATTHelp {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private ConnectCallback connectCallback;
    private ProgressDialog progressDialog;

    private static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public BluetoothGATTHelp(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(ConnectCallback callback) {
        this.connectCallback = callback;

        // Show the progress dialog
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage("Connecting...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
//            progressDialog.dismiss();
            connectCallback.onFailure("Bluetooth not supported");
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
//            progressDialog.dismiss();
            connectCallback.onFailure("Bluetooth not enabled");
            return;
        }

        // Find the target Bluetooth device
        BluetoothDevice device = null;

        @SuppressLint("MissingPermission") Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bondedDevice : bondedDevices) {
            if (bondedDevice.getName().equals("HC-05")) { //
                device = bondedDevice;
                break;
            }
        }

        // Check if the device was found
        if (device == null) {
//            progressDialog.dismiss();
            connectCallback.onFailure("Device not found");
            return;
        }

        // Connect to the device
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    @SuppressLint("MissingPermission")
    public void toggleLED(boolean state) {
        if (characteristic != null) {
            byte[] value = state ? "1".getBytes() : "0".getBytes();
            characteristic.setValue(value);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Device connected, discover services
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Device disconnected, handle cleanup or reconnection logic
                bluetoothGatt.close();
                bluetoothGatt = null;
                characteristic = null;
                connectCallback.onFailure("Device disconnected");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Services discovered, find the characteristic
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                }

                if (characteristic != null) {
                    // Characteristic found, notify the connection success
                    progressDialog.dismiss();
                    connectCallback.onConnected();
                } else {
                    // Characteristic not found, notify failure
                    gatt.disconnect();
                    progressDialog.dismiss();
                    connectCallback.onFailure("Characteristic not found");
                }
            } else {
                // Error discovering services, notify failure
                gatt.disconnect();
                progressDialog.dismiss();
                connectCallback.onFailure("Error discovering services");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic write success
            } else {
                // Characteristic write failure
            }
        }
    };

    public interface ConnectCallback {
        void onConnected();
        void onFailure(String message);
    }
}