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
        String readBT = bluetoothHelper.getReadMessage();
        Log.d("arduinoOOOOO", "CODE" + readBT);
        Toast.makeText(ReceiveParcel.this, "READ ARDUINO " + readBT, Toast.LENGTH_SHORT).show();

        //only do this after arduino sensor confirms it has parcel inside
//        if this doesnt work put in bthelper run and save it in variables then get it from there
        if (readBT.equals("Mobile")) {
            Intent intent = new Intent(ReceiveParcel.this, CourierMobileWallet.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        } else if (readBT.equals("AA")) {
            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        } else if (readBT.equals("BB")) {
            Intent intent = new Intent(ReceiveParcel.this, CashOnDeliveryActivity.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
        } else if (readBT.equals("CC")) {
            Intent intent = new Intent(ReceiveParcel.this, SuccessActivity.class);
            intent.putExtra("trackingID", sampleInputData);
            startActivity(intent);
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
        return sampleInputData;
    }
}

