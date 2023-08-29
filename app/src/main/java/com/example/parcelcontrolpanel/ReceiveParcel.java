package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class ReceiveParcel extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    Context context = this;
    String sampleInputData;
    String phoneNo;
    BluetoothHelper bluetoothHelper = new BluetoothHelper(context, "HC-05", "00:22:12:00:3C:EA");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_parcel);
        Intent intent = getIntent();

        sampleInputData = intent.getExtras().getString("trackingID");
        //if not connected reconnect
        bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
            @Override
            public void onConnected() {
                getTracking();
                //WAIT FOR SENSOR TO OKAY AND PARCEL DROPPED
                //IF DROPPED NA AND SENSOR GOOD IDENTIFY PAYMENT METHOD
                //PROCEED TO NEXT ACTIVITY - MOBILE PAYMENT - THANK YOU IF PREPAID - COD RELEASE PAYMENT
                //IN APPSCRIPT MAKE AN IF VARIABLE - DROPPED SEND TO APPSCRIPT AND SEND DIFFERENT TEXT "DROPPED OR SECURED PARCEL"

            }

            @Override
            public void onFailure() {
                // Dismiss the progress dialog and show an error message
                Toast.makeText(ReceiveParcel.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });

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
        Toast myToast = Toast.makeText(ReceiveParcel.this, sampleInputData, Toast.LENGTH_LONG);
        myToast.show();
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
//        public class CheckRequest extends AsyncTask<String, Void, String> {
//
//            protected void onPreExecute() {
//            }
//
//            protected String doInBackground(String... arg0) {
//
//                try {
//
//                    // Enter script URL Here
//                    String baseUrl = "https://script.google.com/macros/s/AKfycby3qyrW69TzJvcqtbMI0vu0HE-HgmH5yBiWOAgajcJPoHuC4JXE8UxxD8VrOzzNPUPD/exec";
//                    String action = "checkQRParcel";
//                    String trackingId = sampleInputData;
//
//
//                    // Construct the URL with the parameters
//                    String urlString;
//                    urlString = baseUrl + "?action=" + action + "&trackingId=" + trackingId;
//                    URL url = new URL(urlString);
//
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setReadTimeout(30000 /* milliseconds */);
//                    conn.setConnectTimeout(30000 /* milliseconds */);
//                    conn.setRequestMethod("GET");
////                conn.setDoInput(true);
////                conn.setDoOutput(true);
//
//                    int responseCode = conn.getResponseCode();
//
//                    if (responseCode == HttpsURLConnection.HTTP_OK) {
//                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        StringBuffer sb = new StringBuffer("");
//                        String line = "";
//                        while ((line = in.readLine()) != null) {
//                            sb.append(line);
//                            break;
//                        }
//
//                        in.close();
//                        return sb.toString();
//                    } else {
//                        return new String("false : " + responseCode);
//                    }
//                } catch (Exception e) {
//                    return new String("Exception: " + e.getMessage());
//                }
//
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                int dur = 1000000;
//                getPhoneNumber();
//
//
//                Toast.makeText(getApplicationContext(), "Checking Tracking ID", Toast.LENGTH_LONG).show();
////            Log.i("Info", urlString);
//
//
//                if (result.equals("Tracking ID exists: " + sampleInputData + " and payment method is Mobile Wallet")) {
//                    // Tracking ID exists and payment method is Mobile Wallet
//
//
//
//                } else if (result.equals("Tracking ID exists: " + sampleInputData + " but payment method is not Mobile Wallet")) {
//
//
//
//                } else {
////                    loading.dismiss();
//                    // Tracking ID does not exist or error occurred
//                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
//                }
//
//                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//
//
//                // now by putExtra method put the value in key, value pair key is
//                // tracingID by this key we will receive the value, and put the string
//
//            }
//        }

    }

