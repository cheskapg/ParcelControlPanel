package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSource;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSourcePreview;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import android.telephony.SmsManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

public class InputActivity extends AppCompatActivity {
    Context context = this;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    BluetoothHelper bluetoothHelper = new BluetoothHelper(context, "HC-05", "00:22:12:00:3C:EA");
    //        BluetoothHelper bluetoothHelper = new BluetoothHelper();
    public String compNum;
    String inputData;
    String checkData;
    String urlString;
    String sampleInputData;
    String getParcelId, getPaymentId;
    ImageView checkBtn;
    ProgressDialog loading;
    EditText inputTrackingET;
    private ProgressDialog progressDialog;

    String phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String status = bluetoothHelper.getStatus();
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Connecting...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

//         Show the progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Connect to the Bluetooth device
        // Connect to the Bluetooth device
        bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
            @Override
            public void onConnected() {
//                // Dismiss the progress dialog when connected
                progressDialog.dismiss();
//
            }

            //
            @Override
            public void onFailure() {
//                // Dismiss the progress dialog and show an error message
                progressDialog.dismiss();
                bluetoothHelper.disconnect();
                Toast.makeText(InputActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });

        setContentView(R.layout.activity_input);
        checkBtn = (ImageView) findViewById(R.id.bgButtonInputCheck);
        inputTrackingET = (EditText) findViewById(R.id.etTracking);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(inputTrackingET.getText())) {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    inputTrackingET.setError("Tracking ID required!");

                } else {
                    sampleInputData = inputTrackingET.getText().toString();
                    inputData = inputTrackingET.getText().toString();

                    // Create an instance of CheckRequest and execute it with the tracking ID
                    CheckRequest checkRequestTask = new CheckRequest();
                    checkRequestTask.execute(inputData);

                    loading = ProgressDialog.show(InputActivity.this, "Loading", "please wait", false, true);
                }
                // Create an instance of SendRequest and execute it with the input data

            }

        });

    }

    private String getTracking() {
        return sampleInputData;
    }

    public void getPhoneNumber() {
        String url = String.format("https://script.google.com/macros/s/AKfycbw4a8z6clbdTthJcMCJRahBJE3DH7IBSyA1OO9qovz_uj9z3RBw_h3LBslwvgeLhHzL/exec?action=getPhoneNumberByTracking&trackingId=%s", getTracking());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response (file URL)
                        phoneNo = response;
                        Toast.makeText(getApplicationContext(), "Phone:" + phoneNo, Toast.LENGTH_SHORT).show();
                        SMSHandler.sendSMSMessage(InputActivity.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleInputData + " Scanned and Attempting Delivery");

                        loading.dismiss();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Toast.makeText(getApplicationContext(), "Error" + phoneNo, Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }

    public class CheckRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                // Enter script URL Here
                String baseUrl = "https://script.google.com/macros/s/AKfycby_xghDiiCNLZvCU_Gjntiavcj8zKEoh6bT6PUrz7-e_tZGNoGFPKaNnSS-Qa3vyU13/exec";
                String action = "checkQRParcel";
                String trackingId = inputData;

                sampleInputData = inputData;
                // Construct the URL with the parameters
                urlString = baseUrl + "?action=" + action + "&trackingId=" + trackingId;
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
            getPhoneNumber();

            Toast.makeText(getApplicationContext(), "Checking Tracking ID", Toast.LENGTH_LONG).show();
            Log.i("Info", result);

            Toast.makeText(getApplicationContext(), sampleInputData, Toast.LENGTH_LONG).show();

            if (result.equals("Tracking ID exists: " + sampleInputData + " and payment method is Mobile Wallet")) {
                // Tracking ID exists and payment method is Mobile Wallet
                bluetoothHelper.mobileTrigger();
                // unlock door and wait for mobile payment screen

                loading.dismiss();
                Intent intent = new Intent(InputActivity.this, ReceiveParcel.class);
                intent.putExtra("trackingID", sampleInputData);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Mobile Payment Parcel", Toast.LENGTH_LONG).show();

            } else if (result.equals("Tracking ID exists: " + sampleInputData + " but payment method is not Mobile Wallet")) {
                // Tracking ID exists but payment method is not Mobile Wallet
                //uncomment to enable bluetooth command
                bluetoothHelper.prepaidTrigger();
                String readMessage = bluetoothHelper.getReadMessage();

                Toast.makeText(InputActivity.this, "READ ARDUINO " + readMessage, Toast.LENGTH_SHORT).show();
                loading.dismiss();

                Intent moveToPlaceParcel = new Intent(InputActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleInputData);

                startActivity(moveToPlaceParcel);


                Toast.makeText(InputActivity.this, "READ ARDUINO NOT mobile " + readMessage, Toast.LENGTH_SHORT).show();

            } else if (result.equals("Tracking ID exists: " + sampleInputData + " and payment method is COD 1")) {
                // Tracking ID exists but payment method is COD
                bluetoothHelper.codComp1Trigger();
                Toast.makeText(InputActivity.this, "COMPARTMENT IS 1", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(InputActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleInputData);
                startActivity(moveToPlaceParcel);

            } else if (result.equals("Tracking ID exists: " + sampleInputData + " and payment method is COD 2")) {
                bluetoothHelper.codComp2Trigger();
                Toast.makeText(InputActivity.this, "COMPARTMENT IS 2", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(InputActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleInputData);
                startActivity(moveToPlaceParcel);

            } else {
                loading.dismiss();
                // Tracking ID does not exist or error occurred
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
            }

            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            // now by putExtra method put the value in key, value pair key is
            // tracKingID by this key we will receive the value, and put the string

        }

    }


////addqr table testing
//    public class SendRequest extends AsyncTask<String, Void, String> {
//
//        protected String doInBackground(String... arg0) {
//            try {
//                // Enter script URL Here
//                URL url = new URL("https://script.google.com/macros/s/AKfycbwsaC5SjNgbXN9K29GmI15DwDFIbKUwbWDYHYP5NmzNMH1MXLmZcL-aTPlx8NaL1Zrw/exec");
//
//                JSONObject postDataParams = new JSONObject();
//
//                // Passing input data as parameter
//                postDataParams.put("action", "addQrItem");
//                postDataParams.put("sdata", inputData);
//
//                Log.e("params", postDataParams.toString());
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(30000 /* milliseconds */);
//                conn.setConnectTimeout(30000 /* milliseconds */);
//                conn.setRequestMethod("GET");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.write(getPostDataString(postDataParams));
//
//                writer.flush();
//                writer.close();
//                os.close();
//
//                int responseCode = conn.getResponseCode();
//
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    InputStream inputStream = conn.getInputStream();
//                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                    BufferedReader reader = new BufferedReader(inputStreamReader);
//
//                    String response = reader.readLine();
//
//                    reader.close();
//                    inputStreamReader.close();
//                    inputStream.close();
//                    return response;
//                } else {
//                    return "false : " + responseCode;
//                }
//            } catch (Exception e) {
//                return "Exception: " + e.getMessage();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//
//
//        }
//    }
//    public String getPostDataString(JSONObject params) throws Exception {
//
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//
//        Iterator<String> itr = params.keys();
//
//        while(itr.hasNext()){
//
//            String key= itr.next();
//            Object value = params.get(key);
//
//            if (first)
//                first = false;
//            else
//                result.append("&");
//
//            result.append(URLEncoder.encode(key, "UTF-8"));
//            result.append("=");
//            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
//
//        }
//        return result.toString();
//    }

    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app

//            bluetoothHelper.disconnect();
        Intent a = new Intent(InputActivity.this, MainActivity.class);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
//    private void getCompartmentNum(String tracking) {
//        String trackingId = tracking;
//        Log.d("TRACKING",inputData + "input track");
//
//        String url = String.format("https://script.google.com/macros/s/AKfycbycoJM-I4YdT2oMwlI8ZZY8a9HkqrH1N36Aux_Zqcc6MqG6dPnLiL00QODfjk_ESfEK/exec?action=getCompNum&trackingId=%s", trackingId);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        compNum = response;
//                        Toast myToast = Toast.makeText(InputActivity.this, response, Toast.LENGTH_LONG);
//                        myToast.show();
//                        Log.d("compNum",compNum + "gecomprrespone");
//
//                    }
//
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // Handle error response                }
//                    }
//
//                }
//        );
//        int socketTimeOut = 50000;
//        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        stringRequest.setRetryPolicy(policy);
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(stringRequest);
//    }

}
