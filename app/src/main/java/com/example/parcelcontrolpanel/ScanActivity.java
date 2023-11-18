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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ScanActivity extends AppCompatActivity {
    String scannedData, readBT;
    String checkData;
    Context context = this;

    String urlString;
    String sampleScannedData;
    //    BluetoothHelper bluetoothHelper = new BluetoothHelper(context, "HC-05", "00:22:12:00:3C:EA");
    BluetoothHelper bluetoothHelper;
    private BarcodeDetector barcodeDetector;
    String getParcelId, getPaymentId;
    int brightness;
    String phoneNo;
    private ProgressDialog progressDialog, gettingComp;
    public String compNum = "Empty Comp";

    boolean scanbuttonClicked, checkbuttonClicked;
    ImageView scanBtn, checkBtn;

    public ScanActivity() throws IOException {
    }
    //IF PARCEL IS SCANNED BUT SENSOR IS EMPTY DO NOT PROCEED USE ANDROID
    // get VALUE OF COMPARTMET STATUS AND STORE CHECKCOMPARTMENT IN ARDUINO RETURNS EMPTY 1 AND THEN PARCEL SCANNED, SHOW ERROR SCREEN , SEND NOTIF TO USER TO SWITCH TO MOBILE PAYMENT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        final Activity activity = this;
        bluetoothHelper = BluetoothHelper.getInstance(this, "HC-05", "00:22:12:00:3C:EA");

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

        checkBtn = findViewById(R.id.bgButtonScan);

        checkbuttonClicked = false;
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
        if (!bluetoothHelper.isConnected()) {
            bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
                @Override
                public void onConnected() {
                    // Dismiss the progress dialog when connected
                    progressDialog.dismiss();
                    // Continue with other logic or UI updates
                }

                @Override
                public void onFailure() {
                    progressDialog.dismiss();
                    Toast.makeText(ScanActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressDialog.dismiss();

        }
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkbuttonClicked = true;
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
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            scannedData = result.getContents();
            if (scannedData != null) {
                if (checkbuttonClicked) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new CheckRequest().execute();
                }
            } else {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getTracking() {
        return sampleScannedData;
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
                        SMSHandler.sendSMSMessage(ScanActivity.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + sampleScannedData + " Scanned and Attempting Delivery");


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
                String baseUrl = "https://script.google.com/macros/s/AKfycbzEAgfYgo6a1jNUFsRyFVIcxIF2EllQevrABjMEpfI-JnpGpbysnWTHxPms86i2BKyj/exec";
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

            progressDialog.dismiss();
            int dur = 1000000;
            Log.i("Info", result);

            Toast.makeText(getApplicationContext(),
                    "Checking Barcode", Toast.LENGTH_LONG).show();
            Log.i("Info", result);

            Toast.makeText(getApplicationContext(), sampleScannedData, Toast.LENGTH_LONG).show();


            if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is Mobile Wallet")) {
                getPhoneNumber();

                // Tracking ID exists and payment method is Mobile Wallet
                bluetoothHelper.mobileTrigger();
                // unlock door and wait for mobile payment screen

                Intent intent = new Intent(ScanActivity.this, ReceiveParcel.class);
                intent.putExtra("trackingID", sampleScannedData);
                intent.putExtra("userphone", phoneNo);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Mobile Payment Parcel", Toast.LENGTH_LONG).show();

            } else if (result.equals("Tracking ID exists: " + sampleScannedData + " but payment method is not Mobile Wallet")) {
                getPhoneNumber();

                // Tracking ID exists but payment method is not Mobile Wallet
                //uncomment to enable bluetooth command
                bluetoothHelper.prepaidTrigger();
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("userphone", phoneNo);

                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                startActivity(moveToPlaceParcel);
                String readMessage = bluetoothHelper.getReadMessage();
                Toast.makeText(ScanActivity.this, "READ ARDUINO NOT mobile " + readMessage, Toast.LENGTH_SHORT).show();

            } else if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is COD 1")) {
                getPhoneNumber();

                // Tracking ID exists but payment method is COD
                bluetoothHelper.codComp1Trigger();
                Toast.makeText(ScanActivity.this, "COMPARTMENT IS 1", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("userphone", phoneNo);

                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                startActivity(moveToPlaceParcel);

            } else if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is COD 2")) {
                getPhoneNumber();

                bluetoothHelper.codComp2Trigger();
                Toast.makeText(ScanActivity.this, "COMPARTMENT IS 2", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                moveToPlaceParcel.putExtra("userphone", phoneNo);

                startActivity(moveToPlaceParcel);
                getBluetoothMsg();
            }
            else if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is COD 3")) {
                getPhoneNumber();

                bluetoothHelper.codComp3Trigger();
                Toast.makeText(ScanActivity.this, "COMPARTMENT IS 3", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                moveToPlaceParcel.putExtra("userphone", phoneNo);

                startActivity(moveToPlaceParcel);
                getBluetoothMsg();
            }
            else if (result.equals("Tracking ID exists: " + sampleScannedData + " and payment method is COD 4")) {
                getPhoneNumber();

                bluetoothHelper.codComp4Trigger();
                Toast.makeText(ScanActivity.this, "COMPARTMENT IS 4", Toast.LENGTH_SHORT).show();
                Intent moveToPlaceParcel = new Intent(ScanActivity.this, ReceiveParcel.class);
                moveToPlaceParcel.putExtra("trackingID", sampleScannedData);
                moveToPlaceParcel.putExtra("userphone", phoneNo);

                startActivity(moveToPlaceParcel);
                getBluetoothMsg();
            }else if (result.equals("Tracking ID does not exist " + sampleScannedData)) {

                // Tracking ID does not exist or error occurred
                Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
            }


            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();


            // now by putExtra method put the value in key, value pair key is
            // tracingID by this key we will receive the value, and put the string

        }
    }

    public String getBluetoothMsg() {
        readBT = bluetoothHelper.getReadMessage();
        Log.d("arduinoOOOOO", "CODE" + readBT);
        Toast.makeText(ScanActivity.this, "READ ARDUINO " + readBT, Toast.LENGTH_SHORT).show();

        return readBT;
    }
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app

//            bluetoothHelper.disconnect();
        Intent a = new Intent(ScanActivity.this, MainActivity.class);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
