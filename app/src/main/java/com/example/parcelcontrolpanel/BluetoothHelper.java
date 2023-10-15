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
    //ISSUE WITH PIN ENTERING AUTO
    private static final String TAG = "BluetoothHelper";
    private static final int CONNECTING_STATUS = 1;
    private static final int MESSAGE_READ = 2;
    public String Status;
    public InputStream mmInStream;

    private BluetoothSocket mmSocket;
    private ConnectedThread connectedThread;
    private CreateConnectThread createConnectThread;
    private Handler handler;
    private boolean isConnected = false;
    private final String BLE_PIN = "1234";
    private ConnectCallback connectCallback;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private InputStream inputStream;
    private boolean stopWorker;
    public String readArduino;

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
    public BluetoothHelper(Context context, String deviceName, String deviceAddress) {
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
    @SuppressLint("MissingPermission")
    public void connectToDevice(ConnectCallback callback) {
        this.connectCallback = callback;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        // Device is already paired, proceed with connection
        createConnectThread = new CreateConnectThread(device);
        createConnectThread.start();
    }
    public interface ConnectCallback {
        void onConnected();

        void onFailure();
    }

    public void codComp1Trigger() {

        if (connectedThread != null) {
            connectedThread.write("A");
            Log.e("SEND COMMAND", "ARDUINO A");

        } else {
            reconnectToDevice();
            connectedThread.write("A");
        }
    }
    public void codComp2Trigger() {

        if (connectedThread != null) {
            connectedThread.write("B");
            Log.e("SEND COMMAND", "ARDUINO B");
        } else {
            reconnectToDevice();
            connectedThread.write("B");
        }
    }
    public void prepaidTrigger() {

        if (connectedThread != null) {
            connectedThread.write("C");
        } else {
            reconnectToDevice();
            connectedThread.write("C");
        }
    }
    public void mobileTrigger() {
        if (connectedThread != null) {
            connectedThread.write("D");
        } else {
            reconnectToDevice();
            connectedThread.write("D");
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
    private void beginListenForData() {
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        if (inputStream != null) {
                            int bytesAvailable = inputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == '\n') {
                                        String receivedMessage = new String(readBuffer, 0, readBufferPosition);
                                        readBufferPosition = 0;
                                        Log.e("RECEIVED", "RECEIVE" + receivedMessage);
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
    private class CreateConnectThread extends Thread {
        private BluetoothDevice bluetoothDevice;
        public CreateConnectThread(BluetoothDevice device) {
            bluetoothDevice = device;
        }
        @SuppressLint("MissingPermission")
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
            if (mmInStream == null) {
                Log.e("ConnectedThread", "Input stream is null");
                return;
            }

            byte[] buffer = new byte[1024];
            int bytes;
            beginListenForData();

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read data from the InputStream
                    bytes = mmInStream.read(buffer);
                    getReadMessage();
                    // Process the received data
                    if (bytes > 0) {
                        String readMessage = new String(buffer, 0, bytes);
                        Log.e("ARDUINO Message", readMessage);
                        readArduino = readMessage;
                        getReadMessage();

                        if (readMessage.equals("Mobile")) {                    getReadMessage();


                            readArduino = "Mobile";
                        } else if (readMessage.equals("AA")) {                    getReadMessage();

                            readArduino = "AA";
                        } else if (readMessage.equals("BB")) {
                            getReadMessage();

                            readArduino = "BB";
                        } else if (readMessage.equals("CC")) {
                            getReadMessage();

                            readArduino = "CC";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("ERROR BTHELP", "e" + e);
                    break;
                }
            }
            // Keep listening to the InputStream until an exception occurs
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
    public String getReadMessage() {
        String getMessage = readArduino;
        return getMessage;
    }
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        //        public InputStream mmInStream;
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