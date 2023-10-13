package com.example.parcelcontrolpanel;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSource;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSourcePreview;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ScanActivity extends AppCompatActivity {
    String scannedData;
    String checkData;
    Context context = this;

    String urlString;
    String sampleScannedData;
    BluetoothHelper bluetoothHelper = new BluetoothHelper(context, "HC-05", "00:22:12:00:3C:EA");
    private BarcodeDetector barcodeDetector;
    String getParcelId, getPaymentId;
    int brightness;
    String phoneNo;
    private ProgressDialog progressDialog;
    public String compNum;

    boolean scanbuttonClicked, checkbuttonClicked;
    ImageView scanBtn, checkBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        final Activity activity =this;
        String status = bluetoothHelper.getStatus();
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //barcode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
//        scanBtn = (Button)findViewById(R.id.dat);
        checkBtn = findViewById(R.id.bgButtonScan);

//        scanbuttonClicked =false;
        checkbuttonClicked=false;
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        context = getApplicationContext();
//        brightness =
//                Settings.System.getInt(context.getContentResolver(),
//                        Settings.System.SCREEN_BRIGHTNESS, 0);
//        Settings.System.putInt(context.getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS, 100);
        if (!barcodeDetector.isOperational()) {
            Toast.makeText(this, "Could not set up the barcode detector", Toast.LENGTH_SHORT).show();
            return;
        }
        // Connect to the Bluetooth device
        bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
            @Override
            public void onConnected() {
                // Dismiss the progress dialog when connected
                progressDialog.dismiss();

                // Continue with other logic or UI updates
            }

            @Override
            public void onFailure() {
                // Dismiss the progress dialog and show an error message
                progressDialog.dismiss();
//                bluetoothHelper.disconnect();
                Toast.makeText(ScanActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ScanActivity.this, MainActivity.class);

                startActivity(intent);            }
        });


//        scanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                scanbuttonClicked =true;
//                IntentIntegrator integrator = new IntentIntegrator(activity);
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//                integrator.setPrompt("Scan");
//                integrator.setBeepEnabled(true);
//                integrator.setCameraId(1);
//                integrator.setBarcodeImageEnabled(false);
//                integrator.initiateScan();
//            }
//        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkbuttonClicked =true;
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Check");
                integrator.setBeepEnabled(true);
                integrator.setCameraId(1);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null) {
            scannedData = result.getContents();
            if (scannedData != null) {
//                if(scanbuttonClicked) {
//                    // Here we need to handle scanned data...
//                    new SendRequest().execute();
//                }
                if(checkbuttonClicked){
                    new CheckRequest().execute();
                }

            }else {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private String getTracking() {
        return sampleScannedData;
    }
    public void getPhoneNumber(){
        String url = String.format("https://script.google.com/macros/s/AKfycbz0HTpS_z0h5CeN0mgcCrSWh4DXtwzz3oT5hN10QO7nGUQmuy2tc0xDXTtiHIkrysZo/exec?action=getPhoneNumberByTracking&trackingId=%s", getTracking());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response (file URL)
                        phoneNo = response;
                        Toast.makeText(getApplicationContext(),"Phone:" +phoneNo, Toast.LENGTH_SHORT).show();
                        SMSHandler.sendSMSMessage(ScanActivity.this, phoneNo, "ParcelPal SMS Notification: Parcel-"+ sampleScannedData +" Scanned and Attempting Delivery");




                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Toast.makeText(getApplicationContext(),"Error" + phoneNo, Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }
    public class CheckRequest extends AsyncTask<String, Void, String>
    {
        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                getCompartmentNum(scannedData);

                // Enter script URL Here
                String baseUrl = "https://script.google.com/macros/s/AKfycbycoJM-I4YdT2oMwlI8ZZY8a9HkqrH1N36Aux_Zqcc6MqG6dPnLiL00QODfjk_ESfEK/exec";
                String action = "checkQRParcel";
                String trackingId = scannedData;
                sampleScannedData = scannedData;
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
            getPhoneNumber();

            int dur = 1000000;
            Toast.makeText(getApplicationContext(), "Checking Barcode",Toast.LENGTH_LONG).show();
            Log.i("Info", urlString);

            Toast.makeText(getApplicationContext(), sampleScannedData,Toast.LENGTH_LONG).show();


            if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is Mobile Wallet")) {
                // Tracking ID exists and payment method is Mobile Wallet
//                bluetoothHelper.mobileTrigger();
                // unlock door and wait for mobile payment screen

                Intent intent = new Intent(ScanActivity.this, ReceiveParcel.class);
                intent.putExtra("trackingID", sampleScannedData);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Mobile Payment Parcel", Toast.LENGTH_LONG).show();

            }
            else if (result.equals("Tracking ID exists: " + sampleScannedData + " but payment method is not Mobile Wallet")) {
                // Tracking ID exists but payment method is not Mobile Wallet
                //uncomment to enable bluetooth command
                //bluetoothHelper.prepaidTrigger();

                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);

                startActivity(moveToPlaceParcel);


                String readMessage = bluetoothHelper.getReadMessage();
                Toast.makeText(ScanActivity.this, "READ ARDUINO NOT mobile " + readMessage, Toast.LENGTH_SHORT).show();

            }
            else if (result.equals("Tracking ID exists: " + sampleScannedData + " but payment method is COD")) {
                // Tracking ID exists but payment method is COD

                Log.i("COMPNUM", "comp" + compNum);


                // unlock door solenoid change value according to arduino variable for pins
                if (compNum.equals("1")) {
//                    bluetoothHelper.codComp1Trigger();
                    Toast.makeText(ScanActivity.this, "COMPARTMENT IS 1", Toast.LENGTH_SHORT).show();
                }
                if (compNum.equals("2")) {
//                    bluetoothHelper.codComp2Trigger();
                    Toast.makeText(ScanActivity.this, "COMPARTMENT IS 2", Toast.LENGTH_SHORT).show();
                }
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                startActivity(moveToPlaceParcel);
            }

            else {
                // Tracking ID does not exist or error occurred
                Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
            }


           Toast.makeText(getApplicationContext(), result,Toast.LENGTH_LONG).show();


            // now by putExtra method put the value in key, value pair key is
            // tracingID by this key we will receive the value, and put the string

        }
    }
    private void getCompartmentNum(String tracking) {
        String trackingId = tracking;
        Log.d("TRACKING",trackingId + "input track");

        String url = String.format("https://script.google.com/macros/s/AKfycbycoJM-I4YdT2oMwlI8ZZY8a9HkqrH1N36Aux_Zqcc6MqG6dPnLiL00QODfjk_ESfEK/exec?action=getCompNum&trackingId=%s", trackingId);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        compNum = response;
                        Toast myToast = Toast.makeText(ScanActivity.this, response, Toast.LENGTH_LONG);
                        myToast.show();
                        Log.d("compNum",compNum + "gecomprrespone");

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response                }
                    }

                }
        );
        int socketTimeOut = 50000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

//    public class SendRequest extends AsyncTask<String, Void, String>
//    {
//
//
//        protected void onPreExecute(){}
//
//        protected String doInBackground(String... arg0) {
//
//            try{
//
//                //Enter script URL Here
//                URL url = new URL("https://script.google.com/macros/s/AKfycbwsaC5SjNgbXN9K29GmI15DwDFIbKUwbWDYHYP5NmzNMH1MXLmZcL-aTPlx8NaL1Zrw/exec");
//
//                JSONObject postDataParams = new JSONObject();
//
//                //int i;
//                //for(i=1;i<=70;i++)
//                //    String usn = Integer.toString(i);
//                //Passing scanned code as parameter
//                postDataParams.put("action","addQrItem");
//
//                postDataParams.put("sdata",scannedData);
//
//
//                Log.e("params",postDataParams.toString());
//
//                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//                conn.setReadTimeout(30000 /* milliseconds */);
//                conn.setConnectTimeout(30000 /* milliseconds */);
//                conn.setRequestMethod("GET");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(getPostDataString(postDataParams));
//
//                writer.flush();
//                writer.close();
//                os.close();
//
//                int responseCode=conn.getResponseCode();
//
//                if (responseCode == HttpsURLConnection.HTTP_OK) {
//
//                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    StringBuffer sb = new StringBuffer("");
//                    String line="";
//
//                    while((line = in.readLine()) != null) {
//
//                        sb.append(line);
//                        break;
//                    }
//
//                    in.close();
//                    return sb.toString();
//
//                }
//                else {
//                    return new String("false : "+responseCode);
//                }
//            }
//            catch(Exception e){
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();
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
}
