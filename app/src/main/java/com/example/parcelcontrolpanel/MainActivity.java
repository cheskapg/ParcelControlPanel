package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;


public class MainActivity extends AppCompatActivity {
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    ImageView ScanButton, InputButton;
    TextClock dateClock;
    String readBT, phoneNo , compartmentStatus;
    BluetoothHelper bluetoothHelper; //bluetooth to receive check compartments

    Button Bluetooth, SMSButton, btclass, ExitApp, wifi;

    //    BluetoothHelper bluetoothHelper = new BluetoothHelper("HC-05", "00:22:12:00:3C:EA");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //reecently added for receiving sensor info from compartment
        bluetoothHelper = BluetoothHelper.getInstance(this, "HC-05", "00:22:12:00:3C:EA");
        String status = bluetoothHelper.getStatus();

        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();

        if (!bluetoothHelper.isConnected()) {
            bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
                @Override
                public void onConnected() {
                    // Dismiss the progress dialog when connected
                    // Continue with other logic or UI updates
                }

                @Override
                public void onFailure() {
                    Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        }
        getBluetoothMsg();
        readBT = getBluetoothMsg();
        // Start the AsyncTask to read Bluetooth messages and handle the logic
        new BluetoothMessageTask().execute();
        //---------------------------------------------------------------------------------------------
        // Retrieve Device Policy Manager so that we can check whether we can
// lock to screen later
//        mAdminComponentName = new ComponentName(this,AppAdminReceiver.class);
//        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//        if(mDevicePolicyManager.isDeviceOwnerApp(getPackageName())){
//            // App is whitelisted
//            setDefaultCosuPolicies(true);
//        }
//        else {
//            // did you provision the app using <adb shell dpm set-device-owner ...> ?
//        }

        Bluetooth = (Button) findViewById(R.id.Bluetooth);
        SMSButton = (Button) findViewById(R.id.SMSButton);
        ExitApp = (Button) findViewById(R.id.exitApp);
        wifi = (Button) findViewById(R.id.WifiSetup);
        btclass = (Button) findViewById(R.id.Bluetoothclass);

        dateClock = (TextClock) findViewById(R.id.dateClock);
        ScanButton = (ImageView) findViewById(R.id.bgButtonScan);
        InputButton = (ImageView) findViewById(R.id.bgButtonInput);
//        bluetoothHelper.connectToDevice();


        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {

                    Intent moveToScanActivity = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(moveToScanActivity);
                } else {

                    Intent openWirelessSettings = new Intent(MainActivity.this, WifiActivity.class);

                    startActivity(openWirelessSettings);
                }

            }
        });
        InputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable()) {
                    Intent moveToInpActivity = new Intent(MainActivity.this, InputActivity.class);
                    startActivity(moveToInpActivity);
                } else {

                    Intent openWirelessSettings = new Intent(MainActivity.this, WifiActivity.class);

                    startActivity(openWirelessSettings);
                }
            }
        });

        btclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent moveToScanActivity = new Intent(MainActivity.this, BluetoothClassTest.class);
                startActivity(moveToScanActivity);

            }
        });
        SMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toSMSButton = new Intent(MainActivity.this, ReceiveParcel.class);
                startActivity(toSMSButton);

            }
        });


        Bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toBT = new Intent(MainActivity.this, BluetoothMain.class);
                startActivity(toBT);

            }
        });
        ExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();


            }
        });
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openWirelessSettings = new Intent(MainActivity.this, WifiActivity.class);

                startActivity(openWirelessSettings);
            }
        });

        checkCompartmentExisting();


    }
//
//    @Override
//    protected void onStart() {
//        // Consider locking your app here or by some other mechanism
//// Active Manager is supported on Android M
//        super.onStart();
//        if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
//            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
//                    setDefaultCosuPolicies(true);
//                    startLockTask();
//                }
//            }
//        }
//    }
//
////      protected void unlockApp(){
////             ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
////
////         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////             if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) {
////                 stopLockTask();
////             }
////         }
////         setDefaultCosuPolicies(false);
////    }
//    private void setDefaultCosuPolicies(boolean active){
//
//        // Set user restrictions
////        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
////        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
////        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
//
////        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
////        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);
//
//        // Disable keyguard and status bar
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);
//        }
//
////         Enable STAY_ON_WHILE_PLUGGED_IN
////        enableStayOnWhilePluggedIn(active);
//
//        // Set system update policy
//        if (active){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
//            }
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,null);
//            }
//        }
//
//        // set this Activity as a lock task package
//        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,active ? new String[]{getPackageName()} : new String[]{});
//
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
//        intentFilter.addCategory(Intent.CATEGORY_HOME);
//        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
//
//        if (active) {
//            // set Cosu activity as home intent receiver so that it is started
//            // on reboot
//            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter, new ComponentName(getPackageName(), MainActivity.class.getName()));
//        } else {
//            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());
//        }
//    }
//
//    private void setUserRestriction(String restriction, boolean disallow){
//        if (disallow) {
//            mDevicePolicyManager.addUserRestriction(mAdminComponentName,restriction);
//        } else {
//            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,restriction);
//        }
//    }

//    private void enableStayOnWhilePluggedIn(boolean enabled){
//        if (enabled) {
//            mDevicePolicyManager.setGlobalSetting(mAdminComponentName,Settings.Global.STAY_ON_WHILE_PLUGGED_IN,Integer.toString(BatteryManager.BATTERY_PLUGGED_AC| BatteryManager.BATTERY_PLUGGED_USB| BatteryManager.BATTERY_PLUGGED_WIRELESS));
//        } else {
//            mDevicePolicyManager.setGlobalSetting(mAdminComponentName,Settings.Global.STAY_ON_WHILE_PLUGGED_IN,"0");
//        }
//    }


    private class BluetoothMessageTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String readBT = getBluetoothMsg();

            // Perform the Bluetooth message reading in a loop until a condition is met
            while (!isCancelled()) {
                readBT = getBluetoothMsg();
                Log.d("MainActivity", readBT + "--e");



                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
//
        @Override
        protected void onPostExecute(String result) {

                if (readBT.equals("empty 1,empty 2,empty 3,empty 4")) {
                    Log.d("READINGBT", readBT + "--e");

                }

        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public String getBluetoothMsg() {
        readBT = bluetoothHelper.getReadMessage();

        return readBT;
    }
    private void checkCompartmentExisting() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbxe60Ea0TWyGRRig0LemVXLYN_KWBV_QJ6gPZyfiXIIJXsrDLPOqQHk2up0B2Nv_DIu/exec?action=checkExistingComp",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //if response disable 1 means 1 is used and then add && to check if sensors are showing emptyx

                        if (response.equals("disable 1")) {
                            sendCompartmentStatus("1");
                            compartmentStatus ="disable 1";

                        } else if (response.equals("disable 2")) {
                            sendCompartmentStatus("2");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 2 is empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 3")) {
                            sendCompartmentStatus("3");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 3 is empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 4")) {
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 4 is empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 2")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("2");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1 and 2 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 3")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("3");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1 and 3 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 4")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1 and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 2,disable 3")) {
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("3");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 2 and 3 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 2,disable 4")) {
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 2 and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 3,disable 4")) {
                            sendCompartmentStatus("3");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 3 and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 2,disable 3")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("3");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1, 2, and 3 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 2,disable 4")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1, 2, and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 3,disable 4")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("3");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 1, 3, and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 2,disable 3,disable 4")) {
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("3");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: Compartment 2, 3, and 4 are empty. Please insert payment on compartment accordingly");
                        } else if (response.equals("disable 1,disable 2,disable 3,disable 4")) {
                            sendCompartmentStatus("1");
                            sendCompartmentStatus("2");
                            sendCompartmentStatus("3");
                            sendCompartmentStatus("4");
                            SMSHandler.sendSMSMessage(MainActivity.this, phoneNo, "ParcelPal SMS Notification: All compartments are empty. Please insert payment on compartment accordingly");
                        } else {
                            // Handle other cases if needed
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                    }
                }
        );
        int socketTimeOut = 50000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
    private void sendCompartmentStatus(String compartmentNum) {
        String url ="https://script.google.com/macros/s/AKfycbzEAgfYgo6a1jNUFsRyFVIcxIF2EllQevrABjMEpfI-JnpGpbysnWTHxPms86i2BKyj/exec?action=sensor"+compartmentNum;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //SEND SMS HERE  response is phone num to use for sms
                        Log.e("SEND COMMAND", url + "e");
                        phoneNo = response;


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                    }
                }
        );


        int socketTimeOut = 50000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


}