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
    String trackingID, readBT;
    private boolean shouldContinue = true;
    BluetoothHelper bluetoothHelper ;
    ;
    private static final long DELAY_TIME = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_parcel);
        getTracking();
        BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance(context, "HC-05", "00:22:12:00:3C:EA");
        getBluetoothMsg();
        readBT = getBluetoothMsg();
        if (!bluetoothHelper.isConnected()) {
            bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
                @Override
                public void onConnected() {
                    getBluetoothMsg();

                    // Use a Handler to run checkCompartmentExisting with a 2-minute interval
                    final int interval = 3 * 1000; // 2 minutes in milliseconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("HANDLER", "NEXT before if ACT"+ readBT);

                            if (shouldContinue) {

                                if (readBT != null) {
                                    Intent intent;
                                    if (readBT.equals("Mobile")) {
                                        intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
                                        shouldContinue = false;

                                        intent.putExtra("trackingID", sampleInputData);
                                        intent.putExtra("userphone", phoneNo);
                                        startActivity(intent);

                                    } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
                                        shouldContinue = false;

                                        openCODwithDelay();

                                    } else if (readBT.equals("EE")) {
                                        finish();
                                        shouldContinue = false;

                                        openSuccessActivityWithDelay();

                                    } else {
                                        // Handle other cases or show an error message
                                        return;
                                    }

                                } else {
                                    // Handle the case when the AsyncTask was cancelled or no valid result was obtained
                                    Toast.makeText(ReceiveParcel.this, "Failed to read Bluetooth message", Toast.LENGTH_SHORT).show();
                                }                                Log.e("HANDLER", "NEXT ACT"+ readBT);

                                // Schedule the next execution after the interval
                                new Handler().postDelayed(this, interval);
                            }
                        }
                    }, interval);
                }


                @Override
                public void onFailure() {
                    Toast.makeText(ReceiveParcel.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        }        // Start the AsyncTask to read Bluetooth messages and handle the logic
//        new BluetoothMessageTask().execute();
        //only do this after arduino sensor confirms it has parcel inside
//        if this doesnt work put in bthelper run and save it in variables then get it from there


    }

    private void startContinuousTask() {
       getTracking();

        if (readBT != null) {
            Intent intent;
            if (readBT.equals("Mobile")) {
                intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
                shouldContinue = false;

                intent.putExtra("trackingID", sampleInputData);
                intent.putExtra("userphone", phoneNo);
                startActivity(intent);

            } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
                shouldContinue = false;

                openCODwithDelay();

            } else if (readBT.equals("EE")) {
                finish();
                shouldContinue = false;

                openSuccessActivityWithDelay();

            } else {
                // Handle other cases or show an error message
                return;
            }

        } else {
            // Handle the case when the AsyncTask was cancelled or no valid result was obtained
            Toast.makeText(ReceiveParcel.this, "Failed to read Bluetooth message", Toast.LENGTH_SHORT).show();
        }
    }
//    private class BluetoothMessageTask extends AsyncTask<Void, Void, String> {
//        String readBT = getBluetoothMsg();
//        String resp;
//
//        @Override
//        protected String doInBackground(Void... voids) {
//
//            // Perform the Bluetooth message reading in a loop until a condition is met
//            while (!isCancelled()) {
//                readBT = getBluetoothMsg();
//
//                if (readBT.equals("Mobile")) {
//                    return "Mobile";
//                } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
//                    return "AA_BB_CC_DD";
//                } else if (readBT.equals("EE")) {
//                    return "EE";
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
//
//        @Override
//        protected void onPostExecute(String result) {
//            getTracking();
//                Log.d("LOGBT", readBT + "LOG " + result);
//
//            if (readBT != null) {
//                Intent intent;
//                if (readBT.equals("Mobile")) {
//                    intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//                    intent.putExtra("trackingID", sampleInputData);
//                    intent.putExtra("userphone", phoneNo);
//                    startActivity(intent);
//                } else if (readBT.equals("AA") || readBT.equals("BB") || readBT.equals("CC") || readBT.equals("DD")) {
//                    openCODwithDelay();
//
//                } else if (readBT.equals("EE")) {
//                    finish();
//                    openSuccessActivityWithDelay();
//
//                } else {
//                    // Handle other cases or show an error message
//                    return;
//                }
//
//            } else {
//                // Handle the case when the AsyncTask was cancelled or no valid result was obtained
//                Toast.makeText(ReceiveParcel.this, "Failed to read Bluetooth message", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

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
        }, 10000);
    }
}
