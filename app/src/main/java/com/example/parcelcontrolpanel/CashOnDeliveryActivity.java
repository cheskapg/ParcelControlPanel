package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class CashOnDeliveryActivity extends AppCompatActivity {
    String sampleInputData;
    String phoneNo;
    String trackingID;
    ImageView received, received2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_on_delivery);
        getPhoneNumber();
        getTracking();
        received = (ImageView) findViewById(R.id.received);
        received2 = (ImageView) findViewById(R.id.bgReqMobile);
        received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toSucc = new Intent(CashOnDeliveryActivity.this, SuccessActivity.class);
                startActivity(toSucc);

            }
        });
        received2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toSucc = new Intent(CashOnDeliveryActivity.this, SuccessActivity.class);
                startActivity(toSucc);

            }
        });


//        SMSHandler.sendSMSMessage(CashOnDeliveryActivity.this, getPhoneNo(), "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Payment has been released");
    }

    //    private String getPhoneNo() {
//        Intent intent = getIntent();
//        phoneNo = intent.getStringExtra("userphone");
//        Toast myToast = Toast.makeText(CashOnDeliveryActivity.this, "Phone Cash " + phoneNo, Toast.LENGTH_LONG);
//        myToast.show();
//        Log.d("PHone", "CODE" + phoneNo);
//        return phoneNo;
//    }
    public void getPhoneNumber() {
        String url = String.format("https://script.google.com/macros/s/AKfycbz0HTpS_z0h5CeN0mgcCrSWh4DXtwzz3oT5hN10QO7nGUQmuy2tc0xDXTtiHIkrysZo/exec?action=getPhoneNumberByTracking&trackingId=%s", getTracking());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response (file URL)
                        phoneNo = response;
                        Toast.makeText(getApplicationContext(), "Phone:" + phoneNo, Toast.LENGTH_SHORT).show();
                        SMSHandler.sendSMSMessage(CashOnDeliveryActivity.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Payment has been released");


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Toast.makeText(getApplicationContext(), "Error" + phoneNo, Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private String getTracking() {
        Intent intent = getIntent();
        sampleInputData = intent.getStringExtra("trackingID");
        Toast myToast = Toast.makeText(CashOnDeliveryActivity.this, "received Cash" + sampleInputData, Toast.LENGTH_LONG);
        myToast.show();
        trackingID = sampleInputData;
        Log.d("TRACKING", "CODE" + sampleInputData);
        return sampleInputData;
    }
}