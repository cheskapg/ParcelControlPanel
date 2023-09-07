package com.example.parcelcontrolpanel;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
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

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    ImageView ScanButton, InputButton;
    TextClock dateClock;

    Button Bluetooth,SMSButton, btclass, ExitApp, wifi;
//    BluetoothHelper bluetoothHelper = new BluetoothHelper("HC-05", "00:22:12:00:3C:EA");
public String getAndroidVersion() {
    String release = Build.VERSION.RELEASE;

    int sdkVersion = Build.VERSION.SDK_INT;

    return "Android SDK: " + sdkVersion + " (" + release +")";
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve Device Policy Manager so that we can check whether we can
// lock to screen later
        mAdminComponentName = new ComponentName(this,AppAdminReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//        if(mDevicePolicyManager.isDeviceOwnerApp(getPackageName())){
//            // App is whitelisted
//            setDefaultCosuPolicies(true);
//        }
//        else {
//            // did you provision the app using <adb shell dpm set-device-owner ...> ?
//        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            Log.d("VERSION", "NOT MARSH  "+ getAndroidVersion());
        }
        else{
            Log.d("VERSION", getAndroidVersion());
        }

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
                if (isNetworkAvailable()){

                Intent moveToScanActivity = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(moveToScanActivity);
                }
                else{

                    Intent openWirelessSettings = new Intent(MainActivity.this, WifiActivity.class);

                    startActivity(openWirelessSettings);
                }

            }
        });
        InputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevicePolicyManager != null && mAdminComponentName != null) {
                    mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
                }

                //remove admin
                if(isNetworkAvailable()){
                    Intent moveToInpActivity = new Intent(MainActivity.this, InputActivity.class);
                    startActivity(moveToInpActivity);
                }
                else{

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

    }

    @Override
    protected void onStart() {
        // Consider locking your app here or by some other mechanism
// Active Manager is supported on Android M
        super.onStart();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        if(mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())){
//            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
//                setDefaultCosuPolicies(true);
//                startLockTask();
//            }
//
//        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(1);
            if (!taskInfoList.isEmpty()) {
                ComponentName topActivity = taskInfoList.get(0).topActivity;
                if (!topActivity.getPackageName().equals(getPackageName())) {
                    // Lock the app into the foreground
                    moveTaskToFront();
                }
            }
        }
    }

    private void moveTaskToFront() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(100);
        if (!taskInfoList.isEmpty()) {
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.topActivity.getPackageName().equals(getPackageName())) {
                    am.moveTaskToFront(taskInfo.id, 0);
                    break;
                }
            }
        }
    }

//      protected void unlockApp(){
//             ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//             if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) {
//                 stopLockTask();
//             }
//         }
//         setDefaultCosuPolicies(false);
//    }
    private void setDefaultCosuPolicies(boolean active){

        // Set user restrictions
//        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
//        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
//        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);

//        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
//        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // Disable keyguard and status bar
//        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
//        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

//         Enable STAY_ON_WHILE_PLUGGED_IN
//        enableStayOnWhilePluggedIn(active);

        // Set system update policy
//        if (active){
//            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
//        } else {
//            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,null);
//        }


        // set this Activity as a lock task package
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter, new ComponentName(getPackageName(), MainActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());
        }
    }

    private void setUserRestriction(String restriction, boolean disallow){
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName,restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,restriction);
        }
    }

    private void enableStayOnWhilePluggedIn(boolean enabled){
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName,Settings.Global.STAY_ON_WHILE_PLUGGED_IN,Integer.toString(BatteryManager.BATTERY_PLUGGED_AC| BatteryManager.BATTERY_PLUGGED_USB| BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName,Settings.Global.STAY_ON_WHILE_PLUGGED_IN,"0");
        }
    }





    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}