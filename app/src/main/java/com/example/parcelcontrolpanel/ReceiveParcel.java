package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RecursiveAction;

import javax.net.ssl.HttpsURLConnection;


public class ReceiveParcel extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    Context context = this;
    String sampleInputData;
    String phoneNo;
    private BluetoothMessageTask bluetoothMessageTask;
    BluetoothHelper bluetoothHelper;
    String trackingID, readBT;
    private boolean shouldRunCheckCompartment = true;


    private static final long DELAY_TIME = 10000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_parcel);
        getTracking();

        bluetoothHelper = BluetoothHelper.getInstance(context, "HC-05", "00:22:12:00:3C:EA");
//        if (!bluetoothHelper.isConnected()) {
//            bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
//                @Override
//                public void onConnected() {
//                    Log.e("BTSTATUS", "Connected");
//                    // Use a Handler to run checkCompartmentExisting with a 2-minute interval
//                    final int interval =  1000; //
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            getBluetoothMsg();
//
//                            if (shouldRunCheckCompartment) {
//                                Log.e("HANDLER", "rcvpcl");
//                                if (readBT != null) {
//                                    Intent intent;
//                                    if (readBT.equals("Mobile")) {
//                                        intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//                                        intent.putExtra("trackingID", sampleInputData);
//                                        intent.putExtra("userphone", phoneNo);
//                                        startActivity(intent);
//                                    } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
//                                        openCODwithDelay();
//
//                                    } else if (readBT.equals("EE")) {
//                                        finish();
//                                        openSuccessActivityWithDelay();
//
//                                    } else {
//                                        Log.d("LOGBT", readBT + "else" );
//                                    }
//
//                                } else {
//                                    // Handle the case when the AsyncTask was cancelled or no valid result was obtained
//                                    Toast.makeText(ReceiveParcel.this, "Failed to read Bluetooth message", Toast.LENGTH_SHORT).show();
//                                }
//                                // Schedule the next execution after the interval
//                                new Handler().postDelayed(this, interval);
//                            }
//                        }
//                    }, interval);
//                }
//
//
//                @Override
//                public void onFailure() {
//                    Toast.makeText(ReceiveParcel.this, "Failed to connect", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
        // Start the AsyncTask to read Bluetooth messages and handle the logic
//        bluetoothHelper.TESTtrigger();

        new ReceiveParcel.BluetoothMessageTask().execute();
        // Start the AsyncTask to read Bluetooth messages and handle the logic

//        startOrCheckBluetoothTask();

        //only do this after arduino sensor confirms it has parcel inside
//        if this doesnt work put in bthelper run and save it in variables then get it from there

    }

//    private class BluetoothMessageTask extends AsyncTask<Void, Void, String> {
//
//
//        @Override
//        protected String doInBackground(Void... voids) {
//
//            // Perform the Bluetooth message reading in a loop until a condition is met
//            while (!isCancelled()) {
//                readBT = getBluetoothMsg();
//                Log.d("LOGBT", readBT + "LOG" );
//
//                if (readBT != null) {
//                    Intent intent;
//                    if (readBT.equals("Mobile")) {
//                        intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//                        intent.putExtra("trackingID", sampleInputData);
//                        intent.putExtra("userphone", phoneNo);
//                        startActivity(intent);
//                    } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
//                        openCODwithDelay();
//
//                    } else if (readBT.equals("EE")) {
//                        finish();
//                        openSuccessActivityWithDelay();
//
//                    } else {
//                        Log.d("LOGBT", readBT + "else");
//                    }
//                }
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            return null;
//        }
//        It looks like the issue might be related to the placement of your code and the condition checking inside the while loop. Your current implementation checks the conditions inside the loop, which might lead to multiple actions being triggered in a single loop iteration.
//
//                Here's a modified version of your code to address this issue:

    private class BluetoothMessageTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            // Perform the Bluetooth message reading in a loop until a condition is met
            while (!isCancelled()) {
                readBT = getBluetoothMsg();
                Log.d("LOGBT", readBT + "LOG");
                if (readBT.contains("Mobile")) {
                    return "Mobile";
                } else if (readBT.contains("AA") || readBT.contains("BB") || readBT.contains("CC") || readBT.contains("DD") ) {
                    return "AA_BB_CC_DD";
                } else if (readBT.contains("EE")) {
                    return "EE";
                } else if (readBT.contains("HOME")) {
                    return "HOME";
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            getTracking();

            if (readBT != null) {
                Intent intent = null;

                if (result.contains("Mobile")) {
                    intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
                    intent.putExtra("trackingID", sampleInputData);
                    intent.putExtra("userphone", phoneNo);
                } else if (result.contains("AA") || result.contains("BB") || result.contains("CC") || result.contains("DD")) {
                    openCODwithDelay();
                } else if (result.contains("EE")) {
                    finish();
                    openSuccessActivityWithDelay();
                } else if (result.contains("HOME")) {
                    finish();
                    Toast.makeText(ReceiveParcel.this, "NO PARCEL DETECTED", Toast.LENGTH_SHORT).show();
                    intent = new Intent(ReceiveParcel.this, MainActivity.class);

                }
                else {
                    Log.d("LOGBT", readBT + "else");
                }

                // Outside the if-else block, start the activity if intent is not null
                if (intent != null) {
                    startActivity(intent);
                }
            } else {
                // Handle the case when the AsyncTask was cancelled or no valid result was obtained
                Toast.makeText(ReceiveParcel.this, "Failed to read Bluetooth message", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //WAIT FOR SENSOR TO OKAY AND PARCEL DROPPED
    //IF DROPPED NA AND SENSOR GOOD IDENTIFY PAYMENT METHOD
    //PROCEED TO NEXT ACTIVITY - MOBILE PAYMENT - THANK YOU IF PREPAID - COD RELEASE PAYMENT
    //IN APPSCRIPT MAKE AN IF VARIABLE - DROPPED SEND TO APPSCRIPT AND SEND DIFFERENT TEXT "DROPPED OR SECURED PARCEL"
//        if sensor senses parcel inside -> proceed to CHECKREQUEST mobile wallet if mobile payment if not -> cod release money -> if prepaid thank you
    private String getTracking() {
        Intent intent = getIntent();
        sampleInputData = intent.getStringExtra("trackingID");
        trackingID = sampleInputData;
        Log.d("TRACKING", "CODE" + sampleInputData);
        return sampleInputData;
    }

    public String getBluetoothMsg() {
        readBT = bluetoothHelper.getReadMessage();
        Log.d("arduinoOOOOO", "CODE" + readBT);

        return readBT;
    }

    private void openSuccessActivityWithDelay() {
        // Open the SuccessActivity after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent toSucc = new Intent(ReceiveParcel.this, SuccessActivity.class);
                toSucc.putExtra("userphone", phoneNo);
                toSucc.putExtra("trackingID", sampleInputData);
                startActivity(toSucc);
                finish(); // Optional: close the CashOnDeliveryActivity if needed
            }
        }, DELAY_TIME);
    }

    private void openCODwithDelay() {
        // Open the SuccessActivity after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent toCod = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
                toCod.putExtra("userphone", phoneNo);
                toCod.putExtra("trackingID", sampleInputData);
                startActivity(toCod);
                finish(); // Optional: close the CashOnDeliveryActivity if needed
            }
        }, 30000);
    }

    private void startOrCheckBluetoothTask() {
        if (bluetoothMessageTask == null || bluetoothMessageTask.getStatus() == AsyncTask.Status.FINISHED) {
            // Task is not running or has finished, create a new instance and execute it
            bluetoothMessageTask = new BluetoothMessageTask();
            bluetoothMessageTask.execute();
        } else if (bluetoothMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
            // Task is already running
            // You can choose to do nothing, or handle it based on your requirements
        }
    }

    // Call this method to cancel the BluetoothMessageTask
    private void cancelBluetoothTask() {
        if (bluetoothMessageTask != null && bluetoothMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
            // Task is running, cancel it
            bluetoothMessageTask.cancel(true);
        }
    }

}
