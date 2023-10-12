package com.example.parcelcontrolpanel;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;

public class BluetoothClassTest extends AppCompatActivity {
    Button connect,on, off;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_class_test);
        BluetoothHelper bluetoothHelper = new BluetoothHelper(this, "HC-05", "00:22:12:00:3C:EA");
        connect = (Button) findViewById(R.id.btnConnect);
        on = (Button) findViewById(R.id.btnLedOn);
        off = (Button) findViewById(R.id.btnLedOff);


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothHelper.connectToDevice(new BluetoothHelper.ConnectCallback() {
                    @Override
                    public void onConnected() {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bluetoothHelper.codComp1Trigger();
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothHelper.toggleLEDOFF();

            }
        });
    }
}