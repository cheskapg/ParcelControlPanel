package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    String mobileWalletType, trackingID;
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
        Toast myToast = Toast.makeText(CourierMobileWallet.this, "trackingID"+trackingID, Toast.LENGTH_LONG);
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
                    loading = ProgressDialog.show(CourierMobileWallet.this, "Loading", "please wait", false, true);
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
    private void RequestMobilePayment() {
        Toast myToast = Toast.makeText(CourierMobileWallet.this, "EXECUTING REQUEST "+trackingID, Toast.LENGTH_LONG);
        myToast.show();
        String accountName = etAccountName.getText().toString();
        String accountNumber = etAccountNumber.getText().toString();
        if(othersBool){
            mobileWalletType = etOtherMobileWallet.getText().toString();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbxnqjTtN2cACGcN-6BN2-X4eaP3xj8wutLM1A5Z15kpN_MCI6Na9Lr6s_DJBpKNvoe6/exec?action=addCourierMobileWallet", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
//                startActivity(intent);
                Toast.makeText(getApplicationContext(), "REQUESTING",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CourierMobileWallet.this, WaitingForProofPay.class);
                intent.putExtra("trackingID", trackingID);
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