package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CashOnDeliveryActivity extends AppCompatActivity {
    String sampleInputData;
    String phoneNo;
    String trackingID;
    ImageView received, received2;
    private static final long DELAY_TIME = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_on_delivery);
        getTracking();
        getPhoneNumber();
        CheckRequest checkRequestTask = new CheckRequest();
        checkRequestTask.execute();
        received = (ImageView) findViewById(R.id.received);
        received2 = (ImageView) findViewById(R.id.bgReqMobile);
        received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toSucc = new Intent(CashOnDeliveryActivity.this, SuccessActivity.class);
                toSucc.putExtra("trackingID", getTracking());
                startActivity(toSucc);

            }
        });

        received2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSuccessActivityWithDelay();


            }
        });
        openSuccessActivityWithDelay();

    }

    private void openSuccessActivityWithDelay() {
        // Open the SuccessActivity after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent toSucc = new Intent(CashOnDeliveryActivity.this, SuccessActivity.class);
                toSucc.putExtra("trackingID", getTracking());
                startActivity(toSucc);
                finish(); // Optional: close the CashOnDeliveryActivity if needed
            }
        }, DELAY_TIME);
    }

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

    public class CheckRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                // Enter script URL Here
                String baseUrl = "https://script.google.com/macros/s/AKfycbw0cJezInEDGDAWa7v7SOZAb7Yk3sdgO7VgcbIoSbDuu-Le16x_XCT-CuYf0MyP0L4B/exec";
                String action = "paymentReleased";
                String trackingId = getTracking();

                // Construct the URL with the parameters
                String urlString = baseUrl + "?action=" + action + "&trackingId=" + trackingId;
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(30000 /* milliseconds */);
                conn.setConnectTimeout(30000 /* milliseconds */);
                conn.setRequestMethod("GET");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();
                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            int dur = 1000000;

            Log.i("COD ACTIVITY NOTIF", result);

            Toast.makeText(getApplicationContext(), "COD NOTIF" + sampleInputData, Toast.LENGTH_LONG).show();


        }

    }

}