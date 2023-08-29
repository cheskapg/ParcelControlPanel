package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class WaitingForProofPay extends AppCompatActivity {
    ProgressDialog loading;
    String fileUrl, trackingID;
    ImageView imageProof;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_proof_pay);
        Intent intent = getIntent();

        trackingID = intent.getStringExtra("trackingID");
        Toast.makeText(getApplicationContext(), "WAITING FOR " + trackingID,Toast.LENGTH_LONG).show();
        imageProof = (ImageView) findViewById(R.id.imageProof);
        getPaymentImageUrl();
        loading = ProgressDialog.show(WaitingForProofPay.this, "Loading", "please wait", false, true);
    }

    private String getTracking() {
        return trackingID;
    }
    public void getPaymentImageUrl() {

        String url = String.format("https://script.google.com/macros/s/AKfycbzsZ3xrbuMqUNSqr2mfAO-Ul9RgukopL9eGpAWpXxQB7ZMqFfBvYzRs9UVgU0AME5Rv/exec?action=getFileUrlByTrackingID&trackingID=%s", getTracking());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        fileUrl = response;
                        if (fileUrl == null) {
                            // If fileUrl is still null, wait for a short duration and call the method again
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"file" +url, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(),getTracking(), Toast.LENGTH_SHORT).show();

                                    getPaymentImageUrl();
                                }
                            }, 1000); // Adjust the duration as needed
                        } else {
                            imageProof.setVisibility(View.VISIBLE);
                            String imageUrl = fileUrl;


                            if (!TextUtils.isEmpty(imageUrl)) {
                                Picasso.get().load(imageUrl).into(imageProof, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // Image loading success
                                        loading.dismiss();

                                        Toast.makeText(getApplicationContext(),"Proof of Payment Received", Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Image loading error
                                        Toast.makeText(getApplicationContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // If imageUrl is empty, wait for a short duration and call the method again
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getPaymentImageUrl();
                                    }
                                }, 1000); // Adjust the duration as needed
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error" + fileUrl, Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}