package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;

import com.google.zxing.integration.android.IntentIntegrator;


public class MainActivity extends AppCompatActivity {

    ImageView ScanButton, InputButton;
    TextClock dateClock;
    Button Bluetooth,SMSButton, btclass;
//    BluetoothHelper bluetoothHelper = new BluetoothHelper("HC-05", "00:22:12:00:3C:EA");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bluetooth = (Button) findViewById(R.id.Bluetooth);
        SMSButton = (Button) findViewById(R.id.SMSButton);
        btclass = (Button) findViewById(R.id.Bluetoothclass);

        dateClock = (TextClock) findViewById(R.id.dateClock);
        ScanButton = (ImageView) findViewById(R.id.bgButtonScan);
        InputButton = (ImageView) findViewById(R.id.bgButtonInput);
//        bluetoothHelper.connectToDevice();
        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent moveToScanActivity = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(moveToScanActivity);

            }
        });
        InputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent moveToInpActivity = new Intent(MainActivity.this, InputActivity.class);
                startActivity(moveToInpActivity);

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
    }
}