package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SuccessActivity extends AppCompatActivity {
    String sampleInputData;
    String phoneNo;
    String trackingID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        getPhoneNo();
        getTracking();
        SMSHandler.sendSMSMessage(SuccessActivity.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Received Successfully");
    }

    private String getPhoneNo() {
        Intent intent = getIntent();
        phoneNo = intent.getStringExtra("userphone");
        Toast myToast = Toast.makeText(SuccessActivity.this, "Phone " + phoneNo, Toast.LENGTH_LONG);
        myToast.show();
        Log.d("PHone", "CODE" + phoneNo);
        return phoneNo;
    }

    private String getTracking() {
        Intent intent = getIntent();
        sampleInputData = intent.getStringExtra("trackingID");
        Toast myToast = Toast.makeText(SuccessActivity.this, "received " + sampleInputData, Toast.LENGTH_LONG);
        myToast.show();
        trackingID = sampleInputData;
        Log.d("TRACKING", "CODE" + sampleInputData);
        return sampleInputData;
    }
}