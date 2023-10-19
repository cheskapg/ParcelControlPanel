package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


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
    BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance(context, "HC-05", "00:22:12:00:3C:EA");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_parcel);
        getTracking();
        getBluetoothMsg();
        readBT = getBluetoothMsg();
        // Start the AsyncTask to read Bluetooth messages and handle the logic
        new BluetoothMessageTask().execute();
        //only do this after arduino sensor confirms it has parcel inside
//        if this doesnt work put in bthelper run and save it in variables then get it from there
//        while (true) {
//            readBT = getBluetoothMsg();
//
//            if (readBT.equals("Mobile")) {
//                Intent intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//                break;
//            } else if (readBT.equals("AA") || readBT.equals("BB")) {
//                Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//                break;
//            } else if (readBT.equals("CC")) {
//                Intent intent = new Intent(ReceiveParcel.this, SuccessActivity.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//                break;
//            }
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
    private class BluetoothMessageTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            getTracking();

            String readBT = getBluetoothMsg();

            // Perform the Bluetooth message reading in a loop until a condition is met
            while (!isCancelled()) {
                readBT = getBluetoothMsg();

                if (readBT.equals("Mobile")) {
                    return "Mobile";
                } else if (readBT.equals("AA") || readBT.equals("BB")) {
                    return "AA_BB";
                } else if (readBT.equals("CC")) {
                    return "CC";
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

            if (result != null) {
                Intent intent;
                if (result.equals("Mobile")) {
                    intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
                    intent.putExtra("trackingID", sampleInputData);
                    intent.putExtra("userphone", phoneNo);
                    startActivity(intent);
                } else if (result.equals("AA_BB")) {
                    intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
                    intent.putExtra("userphone", phoneNo);
                    startActivity(intent);

                } else if (result.equals("CC")) {
                    intent = new Intent(ReceiveParcel.this, SuccessActivity.class);
                    intent.putExtra("userphone", phoneNo);

                    startActivity(intent);

                } else {
                    // Handle other cases or show an error message
                    return;
                }
                intent.putExtra("trackingID", sampleInputData);
                startActivity(intent);
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
        Toast myToast = Toast.makeText(ReceiveParcel.this, "received " + sampleInputData, Toast.LENGTH_LONG);
        myToast.show();
        trackingID = sampleInputData;
        Log.d("TRACKING", "CODE" + sampleInputData);
        return sampleInputData;
    }
//    public void getPhoneNumber() {
//        String url = String.format("https://script.google.com/macros/s/AKfycbz0HTpS_z0h5CeN0mgcCrSWh4DXtwzz3oT5hN10QO7nGUQmuy2tc0xDXTtiHIkrysZo/exec?action=getPhoneNumberByTracking&trackingId=%s", getTracking());
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Handle the response (file URL)
//                        phoneNo = response;
//                        Toast.makeText(getApplicationContext(), "Phone:" + phoneNo, Toast.LENGTH_SHORT).show();
//                        SMSHandler.sendSMSMessage(ReceiveParcel.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Payment has been released");
//
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // Handle the error
//                Toast.makeText(getApplicationContext(), "Error" + phoneNo, Toast.LENGTH_SHORT).show();
//            }
//        });
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(stringRequest);
//    }


        public String getBluetoothMsg() {
        readBT = bluetoothHelper.getReadMessage();
        Log.d("arduinoOOOOO", "CODE" + readBT);

        return readBT;
    }
}
