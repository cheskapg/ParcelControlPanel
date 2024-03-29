package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CourierMobileWallet extends AppCompatActivity {

    EditText etAccountName, etAccountNumber, etOtherMobileWallet;
    RadioGroup rgMobileWallet;
    RadioButton rbGcash, rbMaya, rbOthers,mobilewalletRb;
    ImageView ivRequestButton, requesticon;
    String mobileWalletType, trackingID, phoneNo;
    Boolean othersBool;
    ProgressDialog loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_mobile_wallet);
        etAccountName=(EditText) findViewById(R.id.EditTextAccountName);
        etOtherMobileWallet=(EditText) findViewById(R.id.etOtherPayment);
        etAccountNumber=(EditText) findViewById(R.id.EditTextAccountNumber);
        rgMobileWallet=(RadioGroup) findViewById(R.id.payment_type_radio_group);
        rbGcash=(RadioButton) findViewById(R.id.gcash_radio_button);
        rbMaya=(RadioButton) findViewById(R.id.maya_radio_button);
        rbOthers=(RadioButton) findViewById(R.id.others_radio_button);
        ivRequestButton = (ImageView)  findViewById(R.id.bgReqMobile);
        requesticon = (ImageView)  findViewById(R.id.requesticon);
        // receive the value by getStringExtra() method and

        // key must be same which is sent by first activity
        Intent intent = getIntent();
        trackingID = intent.getStringExtra("trackingID");
        getParcelIntent();
//        phoneNo = intent.getStringExtra("userphone");
//        Log.i("PHONE", phoneNo + "courier");

        Toast myToast = Toast.makeText(CourierMobileWallet.this, "trackingID "+trackingID, Toast.LENGTH_LONG);
        myToast.show();

        ivRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Toast myToast = Toast.makeText(CourierMobileWallet.this, "CLICKED", Toast.LENGTH_LONG);
                myToast.show();
                if( TextUtils.isEmpty(etAccountName.getText())&&TextUtils.isEmpty(etAccountNumber.getText())&&(!rbGcash.isChecked() && !rbMaya.isChecked())&& !rbOthers.isChecked())
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountName.setError( "Field required!" );
                    etAccountNumber.setError( "Field required!" );
                    rbGcash.setError( "Field required!" );

                }
                else if( TextUtils.isEmpty(etAccountName.getText())) {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountName.setError("Field required!");
                }
                else if( TextUtils.isEmpty(etAccountNumber.getText()))
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountNumber.setError( "Field required!" );

                }else if((!rbGcash.isChecked() && !rbMaya.isChecked())&& !rbOthers.isChecked())
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    rbGcash.setError( "Field Required" );


                }else{

                    RequestMobilePayment();

                }
            }
        });
        requesticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(CourierMobileWallet.this, "CLICKED", Toast.LENGTH_LONG);
                myToast.show();
                if( TextUtils.isEmpty(etAccountName.getText())&&TextUtils.isEmpty(etAccountNumber.getText())&&(!rbGcash.isChecked() && !rbMaya.isChecked())&& !rbOthers.isChecked())
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountName.setError( "Field required!" );
                    etAccountNumber.setError( "Field required!" );
                    rbGcash.setError( "Field required!" );

                }
                else if( TextUtils.isEmpty(etAccountName.getText())) {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountName.setError("Field required!");
                }
                else if( TextUtils.isEmpty(etAccountNumber.getText()))
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    etAccountNumber.setError( "Field required!" );

                }else if((!rbGcash.isChecked() && !rbMaya.isChecked())&& !rbOthers.isChecked())
                {
                    /**
                     *   You can Toast a message here that the Username is Empty
                     **/

                    rbGcash.setError( "Field Required" );


                }else{
                    RequestMobilePayment();

                }
            }
        });

        rgMobileWallet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.gcash_radio_button:
                        mobileWalletType = "Gcash";
                        othersBool = false;
                        etOtherMobileWallet.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.maya_radio_button:
                        mobileWalletType = "Maya";
                        othersBool = false;

                        etOtherMobileWallet.setVisibility(View.INVISIBLE);
                        break;

                    case R.id.others_radio_button:
                        etOtherMobileWallet.setVisibility(View.VISIBLE);
                        othersBool = true;
                        mobileWalletType = etOtherMobileWallet.getText().toString();

                        break;

                }
            }
        });

    }

    public String getTrackingID() {
        return trackingID;
    }

    public void getParcelIntent() {

        String url = String.format("https://script.google.com/macros/s/AKfycbw4a8z6clbdTthJcMCJRahBJE3DH7IBSyA1OO9qovz_uj9z3RBw_h3LBslwvgeLhHzL/exec?action=getPhoneNumberByTracking&trackingId=%s",             getTrackingID());
        Log.e("StringUrl", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response (file URL)
                        phoneNo = response;
                        Toast.makeText(getApplicationContext(), "Phone:" + phoneNo, Toast.LENGTH_SHORT).show();



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
    private void RequestMobilePayment() {
        loading = ProgressDialog.show(CourierMobileWallet.this, "Loading", "please wait", false, true);
        Toast myToast = Toast.makeText(CourierMobileWallet.this, "EXECUTING REQUEST "+trackingID, Toast.LENGTH_LONG);
        myToast.show();
        String accountName = etAccountName.getText().toString();
        String accountNumber = etAccountNumber.getText().toString();
        if(othersBool){
            mobileWalletType = etOtherMobileWallet.getText().toString();
        }
        SMSHandler.sendSMSMessage(CourierMobileWallet.this, phoneNo, "ParcelPal SMS Notification: Parcel-" + trackingID + " Requesting " + mobileWalletType + " Mobile Payment for " + accountName + " with Account Number: " + accountNumber);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbwQZlD4iHu53rUGN0lucE7TTutLyCESFkuk4uF4XrQKtsZp9fJC3SrGW6qcH92JM5uT/exec?action=addCourierMobileWallet", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
//                startActivity(intent);

                loading.dismiss(); // recently added to dismiss loading
                Toast.makeText(getApplicationContext(), "REQUESTING",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CourierMobileWallet.this, WaitingForProofPay.class);
                intent.putExtra("trackingID", trackingID);
                intent.putExtra("userphone", phoneNo);
                intent.putExtra("accountName", accountName);
                intent.putExtra("accountNumber", accountNumber);

                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();

                params.put("trackingID", trackingID);
                params.put("courierName", accountName);
                params.put("courierMobileWallet", accountNumber);
                params.put("mobileWalletType", mobileWalletType);

                return params;
            }
        };

        int socketTimeOut = 50000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

}