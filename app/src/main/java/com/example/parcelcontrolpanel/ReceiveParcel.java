package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
    String trackingID;
    BluetoothHelper bluetoothHelper = new BluetoothHelper(context, "HC-05", "00:22:12:00:3C:EA");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_parcel);
        getTracking();
        String readMessage = bluetoothHelper.getReadMessage();
        Log.d("arduinoOOOOO","CODE" + readMessage);
        Toast.makeText(ReceiveParcel.this, "READ ARDUINO " + readMessage, Toast.LENGTH_SHORT).show();

        //only do this after arduino sensor confirms it has parcel inside
        if (readMessage == null) {
            Log.d("arduinoOOOOO", "CODE" + readMessage);
        } else if (readMessage.equals("Waiting")) {
            while (readMessage.equals("Waiting")) {
                // Keep waiting until the message is no longer "Waiting"
                // Read the message again
                readMessage= bluetoothHelper.getReadMessage();
            }
        } else if (readMessage.equals("Mobile")) {
            Intent intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        } else if (readMessage.equals("AA")) {
            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        } else if (readMessage.equals("BB")) {
            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        }
//
//        if (readMessage.equals("Mobile")) {
//            Intent intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//            intent.putExtra("trackingID", sampleInputData);
//            startActivity(intent);
//        } else if (readMessage.equals("AA")) {
//            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
//            intent.putExtra("trackingID", sampleInputData);
//            startActivity(intent);
//        } else if (readMessage.equals("BB")) {
//            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
//            intent.putExtra("trackingID", sampleInputData);
//            startActivity(intent);
//        }
//        else if(readMessage.equals(null)){
//            Log.d("arduinoOOOOO","CODE" + readMessage);
//
//        }
//        ReceiveParcel.CheckRequest checkRequestTask = new CheckRequest();
//        checkRequestTask.execute(sampleInputData);
        //if not connected reconnect
//        bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
//            @Override
//            public void onConnected() {
//                getTracking();
//
//
//            }
//
//            @Override
//            public void onFailure() {
//                // Dismiss the progress dialog and show an error message
//                Toast.makeText(ReceiveParcel.this, "Failed to connect", Toast.LENGTH_SHORT).show();
//            }
//        });
//WAIT FOR SENSOR TO OKAY AND PARCEL DROPPED
        //IF DROPPED NA AND SENSOR GOOD IDENTIFY PAYMENT METHOD
        //PROCEED TO NEXT ACTIVITY - MOBILE PAYMENT - THANK YOU IF PREPAID - COD RELEASE PAYMENT
        //IN APPSCRIPT MAKE AN IF VARIABLE - DROPPED SEND TO APPSCRIPT AND SEND DIFFERENT TEXT "DROPPED OR SECURED PARCEL"
//        if sensor senses parcel inside -> proceed to CHECKREQUEST mobile wallet if mobile payment if not -> cod release money -> if prepaid thank you
//        if () {
//
//
//
//        } else {
//                  }

    }

    private String getTracking() {
        Intent intent = getIntent();
        sampleInputData = intent.getStringExtra("trackingID");
        Toast myToast = Toast.makeText(ReceiveParcel.this, "received " + sampleInputData, Toast.LENGTH_LONG);
        myToast.show();
        trackingID = sampleInputData;
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
//                        SMSHandler.sendSMSMessage(ReceiveParcel.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Parcel Secured ");
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
//    }


//    public class CheckRequest extends AsyncTask<String, Void, String> {
//
//        protected void onPreExecute() {
//        }
//
//        protected String doInBackground(String... arg0) {
//
//            try {
//
//                // Enter script URL Here
//                String baseUrl = "https://script.google.com/macros/s/AKfycbyqMBMw5LlgQpMj4jQFMy_agAxRxVO30y0W0Qi6z-eMiXdaD-CxyM-a9pQSGyqgo2_Y/exec";
//                String action = "checkQRParcel";
//                String trackingId = sampleInputData;
//
//
//                // Construct the URL with the parameters
//                String urlString;
//                urlString = baseUrl + "?action=" + action + "&trackingId=" + trackingId;
//                URL url = new URL(urlString);
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(30000 /* milliseconds */);
//                conn.setConnectTimeout(30000 /* milliseconds */);
//                conn.setRequestMethod("GET");
////                conn.setDoInput(true);
////                conn.setDoOutput(true);
//
//                int responseCode = conn.getResponseCode();
//
//                if (responseCode == HttpsURLConnection.HTTP_OK) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    StringBuffer sb = new StringBuffer("");
//                    String line = "";
//                    while ((line = in.readLine()) != null) {
//                        sb.append(line);
//                        break;
//                    }
//
//                    in.close();
//                    return sb.toString();
//                } else {
//                    return new String("false : " + responseCode);
//                }
//            } catch (Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            int dur = 1000000;
////                getPhoneNumber();
//
//
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
////            Log.i("Info", urlString);
//
//            //IF SENSOR HAS SENSED PARCEL IS READY DROPPED
//            if (result.equals("Tracking ID exists: " + sampleInputData + " and payment method is Mobile Wallet")) {
//                // Tracking ID exists and payment method is Mobile Wallet
//                Intent intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//
//
//            } else if (result.equals("Tracking ID exists: " + sampleInputData + " but payment method is not Mobile Wallet")) {
//
//                Intent intent = new Intent(ReceiveParcel.this, SuccessActivity.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//
//
//                //COD
//            } else if (result.equals("Tracking ID exists: " + sampleInputData + " but payment method is COD")) {
//                //RELEASE MONEY ACTIVITY
//                Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
//                intent.putExtra("trackingID", sampleInputData);
//                startActivity(intent);
//
//            } else {
////                    loading.dismiss();
//                // Tracking ID does not exist or error occurred
//                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
//            }
//
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//
//
//            // now by putExtra method put the value in key, value pair key is
//            // tracingID by this key we will receive the value, and put the string
//
//        }
//    }

}

