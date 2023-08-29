package com.example.parcelcontrolpanel;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

//        BluetoothHelper bluetoothHelper = new BluetoothHelper(handler);
//        . Call `connectToDevice()` to connect to a Bluetooth device:
//
//        BluetoothDevice device = ...; // Obtain the BluetoothDevice object
//        bluetoothHelper.connectToDevice(device);

//        2. Call `toggleLED()` to toggle the LED on the Arduino board:
//        bluetoothHelper.toggleLED();
//        3. Call `disconnect()` to disconnect from the connected Bluetooth device:
//
//        bluetoothHelper.disconnect();

//
//THIS WORKS BUT DOES NOT CHECK THE CONNECTION AND RETRY CONTINUOUS AND PAIRS
public class BluetoothHelper {
    private Context context;

    private static final String TAG = "BluetoothHelper";
    private static final int CONNECTING_STATUS = 1;
    private static final int MESSAGE_READ = 2;
    public String Status;
    private BluetoothSocket mmSocket;
    private ConnectedThread connectedThread;
    private CreateConnectThread createConnectThread;
    private Handler handler;
    private boolean isConnected = false;
    private final String BLE_PIN = "1234";
    private ConnectCallback connectCallback;

    private String deviceName;
    private String deviceAddress;
//    public BluetoothHelper(String deviceName, String deviceAddress) {

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevice.setPin(BLE_PIN.getBytes());
                Log.e(TAG, "Auto-entering pin: " + BLE_PIN);
                bluetoothDevice.createBond();
                Log.e(TAG, "Pin entered and request sent...");
            }
        }
    };

    public void registerPairingRequestReceiver() {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
         context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterPairingRequestReceiver() {
        context.unregisterReceiver(broadcastReceiver);
    }
    public BluetoothHelper( Context context, String deviceName, String deviceAddress) {
        this.context = context;

        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                Log.d("CONNECTTEDCONNECTTED", "DEVICE CONNECTTED");
                                Status = "BT CONNECTED";
                                connectCallback.onConnected();

                                break;
                            case -1:
                                Log.d("DISCCONENECTEDENECTED", "DEVICE DISCCONENECTED");
                                Status = "BT NOT CONNECTED";
                                connectCallback.onFailure();


                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString();
                        // Handle received Arduino messages
                        break;
                }
            }
        };
    }
    public String getStatus() {
        return Status;
    }
// public void connectToDevice() {
//        if (!isConnected) {
//
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
//            createConnectThread = new CreateConnectThread(device);
//            createConnectThread.start();
//        }
//    }
//    @SuppressLint("MissingPermission")
//    public void connectToDevice() {
//    if (!isConnected) {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
//
//        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//            // Device is already paired, start connection
//            createConnectThread = new CreateConnectThread(device);
//            createConnectThread.start();
//        } else {
//            // Device is not paired, initiate pairing process
//            registerPairingRequestReceiver();
//            device.createBond();
//        }
//    }
//}

    @SuppressLint("MissingPermission")
    public void connectToDevice(ConnectCallback callback) {
        this.connectCallback = callback;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            // Pair with the device
            //use this to register the hc05 without manually inputting the pin
            registerPairingRequestReceiver();

            if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                try {
                    registerPairingRequestReceiver();

//                    String pin = "1234"; // Replace with your desired PIN
//                    byte[] pinBytes = pin.getBytes();
//                    Method setPinMethod = device.getClass().getMethod("setPin", byte[].class);
//                    setPinMethod.invoke(device, pinBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Method createBondMethod = device.getClass().getMethod("createBond");
                    createBondMethod.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Device is already paired, proceed with connection
            createConnectThread = new CreateConnectThread(device);
            createConnectThread.start();
        }
    }
    public interface ConnectCallback {
        void onConnected();
        void onFailure();
    }
    public void toggleLEDOn() {

        if (connectedThread != null) {
            connectedThread.write("1");
        }
        else{
            reconnectToDevice();
            connectedThread.write("1");
        }
    }

    public void toggleLEDOFF() {
        reconnectToDevice();
        if (connectedThread != null) {
            connectedThread.write("0");
        }
    }

    public void disconnect() {
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
    }
    public void reconnectToDevice() {
        if (!isConnected) {
            connectToDevice(new BluetoothHelper.ConnectCallback() {
                @Override
                public void onConnected() {

                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    private class CreateConnectThread extends Thread {
        private BluetoothDevice bluetoothDevice;

        public CreateConnectThread(BluetoothDevice device) {
            bluetoothDevice = device;
        }

        @SuppressLint("MissingPermission")
//        public void run() {
//            BluetoothSocket tmp = null;
//            try {
//                UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
//                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//                mmSocket = tmp;
//                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                bluetoothAdapter.cancelDiscovery();
//                mmSocket.connect();
//                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
//                isConnected = true;
//                connectedThread = new ConnectedThread(mmSocket);
//                connectedThread.start();
//            } catch (IOException e) {
//                Log.e(TAG, "Failed to connect to device", e);
//                handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
//                isConnected = false;
//                if (mmSocket != null) {
//                    try {
//                        mmSocket.close();
//                    } catch (IOException closeException) {
//                        Log.e(TAG, "Failed to close the socket", closeException);
//                    }
//                }
//            }
//        }
        public void run() {
            BluetoothSocket tmp = null;
            while (!isConnected) {
                try {
                    UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
                    tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                    mmSocket = tmp;
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothAdapter.cancelDiscovery();
                    mmSocket.connect();
                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
                    isConnected = true;
                    connectedThread = new ConnectedThread(mmSocket);
                    connectedThread.start();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to connect to device", e);
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    isConnected = false;
                    if (mmSocket != null) {
                        try {
                            mmSocket.close();
                        } catch (IOException closeException) {
                            Log.e(TAG, "Failed to close the socket", closeException);
                        }
                    }
                    // Wait for a certain period before attempting to reconnect
                    try {
                        Thread.sleep(2000); // Adjust the sleep duration as needed
                    } catch (InterruptedException ex) {
                        Log.e(TAG, "Interrupted while waiting to reconnect", ex);
                    }
                }
            }
        }

        public void cancel() {
            if (mmSocket != null) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close the socket", e);
                }
            }
            isConnected = false;
        }
    }

    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private boolean isLEDOn;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
















//TRIAL CONTINUOUS CONNECT
//
//public class BluetoothHelper {
//
//    private static final String TAG = "BluetoothHelper";
//    private static final int CONNECTING_STATUS = 1;
//    private static final int MESSAGE_READ = 2;
//    public String Status;
//    private BluetoothSocket mmSocket;
//    private ConnectedThread connectedThread;
//    private CreateConnectThread createConnectThread;
//    private Handler handler;
//    private boolean isConnected = false;
//
//    private String deviceName;
//    private String deviceAddress;
//    private BluetoothDevice bluetoothDevice;

//    public BluetoothHelper(String deviceName, String deviceAddress) {
//        this.deviceName = deviceName;
//        this.deviceAddress = deviceAddress;
//
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
//
//        handler = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case CONNECTING_STATUS:
//                        switch (msg.arg1) {
//                            case 1:
//                                Log.d("CONNECTTEDCONNECTTED", "DEVICE CONNECTTED");
//                                Status = "BT CONNECTED";
//                                break;
//                            case -1:
//                                Log.d("DISCCONENECTEDENECTED", "DEVICE DISCCONENECTED");
//                                Status = "BT NOT CONNECTED";
////                                reconnectToDevice();
//                                // Reconnect if disconnected
//                                break;
//                        }
//                        break;
//
//                    case MESSAGE_READ:
//                        String arduinoMsg = msg.obj.toString();
//                        // Handle received Arduino messages
//                        break;
//                }
//            }
//        };
//    }
//
//    public String getStatus() {
//        return Status;
//    }
//
//    @SuppressLint("MissingPermission")
//    public void connectToDevice() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
//
//        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//            // Pair with the device
//            if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
//                try {
//                    String pin = "1234"; // Replace with your desired PIN
//                    byte[] pinBytes = pin.getBytes();
//                    Method setPinMethod = device.getClass().getMethod("setPin", byte[].class);
//                    setPinMethod.invoke(device, pinBytes);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                try {
//                    Method createBondMethod = device.getClass().getMethod("createBond");
//                    createBondMethod.invoke(device);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            // Device is already paired, proceed with connection
//            createConnectThread = new CreateConnectThread(device);
//            createConnectThread.start();
//        }
//    }
//
//    private void reconnectToDevice() {
//        if (!isConnected) {
//            createConnectThread = new CreateConnectThread(bluetoothDevice);
//            createConnectThread.start();
//        }
//    }
//
//    public void toggleLEDOn() {
//        if (isConnected) { // check connection, else connect again
//            if (connectedThread != null) {
//                connectedThread.write("1");
//            }
//        } else {
//            connectToDevice();
//            toggleLEDOn();
//        }
//
//    }
//
//    public void toggleLEDOFF() {
//        if (connectedThread != null) {
//            connectedThread.write("0");
//        }
//    }
//
//    public void disconnect() {
//        if (createConnectThread != null) {
//            createConnectThread.cancel();
//        }
//    }
//
//    private class CreateConnectThread extends Thread {
//        private BluetoothDevice bluetoothDevice;
//
//        public CreateConnectThread(BluetoothDevice device) {
//            bluetoothDevice = device;
//        }
//
//        @SuppressLint("MissingPermission")
//        public void run() {
//            BluetoothSocket tmp = null;
//            while (!isConnected) {
//                try {
//                    UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
//                    tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//                    mmSocket = tmp;
//                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                    bluetoothAdapter.cancelDiscovery();
//                    mmSocket.connect();
//                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
//                    isConnected = true;
//                    connectedThread = new ConnectedThread(mmSocket);
//                    connectedThread.start();
//                } catch (IOException e) {
//                    Log.e(TAG, "Failed to connect to device", e);
//                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
//                    isConnected = false;
//                    if (mmSocket != null) {
//                        try {
//                            mmSocket.close();
//                        } catch (IOException closeException) {
//                            Log.e(TAG, "Failed to close the socket", closeException);
//                        }
//                    }
//                    // Wait for a certain period before attempting to reconnect
//                    try {
//                        Thread.sleep(2000); // Adjust the sleep duration as needed
//                    } catch (InterruptedException ex) {
//                        Log.e(TAG, "Interrupted while waiting to reconnect", ex);
//                    }
//                }
//            }
//        }
//
//        public void cancel() {
//            if (mmSocket != null) {
//                try {
//                    mmSocket.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Failed to close the socket", e);
//                }
//            }
//            isConnected = false;
//        }
//    }
//
//    private class ConnectedThread extends Thread {
//        private BluetoothSocket mmSocket;
//        private InputStream mmInStream;
//        private OutputStream mmOutStream;
//        private boolean isLEDOn;
//
//        public ConnectedThread(BluetoothSocket socket) {
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//            // Get the input and output streams, using temp objects because
//            // member streams are final
//            try {
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) {
//            }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//        }
//
//        public void run() {
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            int bytes = 0; // bytes returned from read()
//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                try {
//                    /*
//                    Read from the InputStream from Arduino until termination character is reached.
//                    Then send the whole String message to GUI Handler.
//                     */
//                    buffer[bytes] = (byte) mmInStream.read();
//                    String readMessage;
//                    if (buffer[bytes] == '\n') {
//                        readMessage = new String(buffer, 0, bytes);
//                        Log.e("Arduino Message", readMessage);
//                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
//                        bytes = 0;
//                    } else {
//                        bytes++;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//        }
//
//        /* Call this from the main activity to send data to the remote device */
//        public void write(String input) {
//            byte[] bytes = input.getBytes(); //converts entered String into bytes
//            try {
//                mmOutStream.write(bytes);
//            } catch (IOException e) {
//                Log.e("Send Error", "Unable to send message", e);
//            }
//        }
//
//        /* Call this from the main activity to shutdown the connection */
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//            }
//        }
//    }
//}
