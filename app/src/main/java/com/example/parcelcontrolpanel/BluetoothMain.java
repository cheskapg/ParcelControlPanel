package com.example.parcelcontrolpanel;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothMain extends AppCompatActivity {
    Button SelectBT;
    public static String readArd;
    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private InputStream inputStream;
    private boolean stopWorker;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_main);
        SelectBT = (Button) findViewById(R.id.selectbt);

        SelectBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toSelectBT = new Intent(BluetoothMain.this, SelectDeviceActivity.class);
                startActivity(toSelectBT);

            }
        });
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setEnabled(true);
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setBackgroundColor(getResources().getColor(R.color.black));

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            Toast myToast = Toast.makeText(this , "Connecting to " + deviceName , Toast.LENGTH_LONG);
            myToast.show();
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,
                    deviceAddress);
            createConnectThread.start();


        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonToggle.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        switch (arduinoMsg.toLowerCase()) {
                            case "AA":
                                imageView.setBackgroundColor(getResources().getColor(R.color.white));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "Waiting":
                                imageView.setBackgroundColor(getResources().getColor(R.color.black));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                        }
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(BluetoothMain.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        // Button to ON/OFF LED on Arduino Board
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String btnState = buttonToggle.getText().toString().toLowerCase();
                beginListenForData();
                switch (btnState) {
                    case "turn on":
                        buttonToggle.setText("Turn Off");
                        // Command to turn on LED on Arduino. Must match with the command in Arduino code
                        cmdText = "1";
                        break;
                    case "turn off":
                        buttonToggle.setText("Turn On");
                        // Command to turn off LED on Arduino. Must match with the command in Arduino code
                        cmdText = "0";
                        break;
                }
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });
    }
    private void beginListenForData() {
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
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
                                    handleMessage(receivedMessage);
                                } else {
                                    readBuffer[readBufferPosition++] = b;
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

    public class CreateConnectThread extends Thread {
        private Context context;

        @SuppressLint("MissingPermission")
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;

            @SuppressLint("MissingPermission") UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {

                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device"+ connectException);
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
            beginListenForData();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            BluetoothSocket tmp = null;

            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read data from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Process the received data
                    if (bytes > 0) {
                        String readMessage = new String(buffer, 0, bytes);
                        readArd = readMessage;
                        Log.e("ARDUINO Message", readMessage);


//                        if (readMessage.equals("Mobile")) {
//
//
//                        } else if (readMessage.equals("AA")) {
//
//
//                        } else if (readMessage.equals("BB")) {
//
//                        } else if (readMessage.equals("CC")) {
//
//                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            // Keep listening to the InputStream until an exception occurs
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    public String getReadArd(String msg){
        msg = readArd;
        return msg;
    }
    private void handleMessage(String message) {
        // Process the received message here
        // You can update UI or perform any other actions based on the message
        // For example:
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(BluetoothMain.this, MainActivity.class);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}

