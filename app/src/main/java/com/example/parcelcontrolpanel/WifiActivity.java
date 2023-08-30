package com.example.parcelcontrolpanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private WifiManager wifiManager;
    private ListView wifiListView;
    private WifiNetworkAdapter wifiNetworkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        wifiListView = findViewById(R.id.wifiListView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiNetworkAdapter = new WifiNetworkAdapter(WifiActivity.this, new ArrayList<>());
        wifiListView.setAdapter(wifiNetworkAdapter);

        wifiListView.setOnItemClickListener((parent, view, position, id) -> {
            ScanResult wifiNetwork = wifiNetworkAdapter.getItem(position);
            connectToWifi(wifiNetwork);
        });

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        scanWifiNetworks();
    }

    private void scanWifiNetworks() {
        if (ActivityCompat.checkSelfPermission(WifiActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WifiActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            performWifiScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performWifiScan();
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    private void performWifiScan() {
        wifiManager.startScan();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ActivityCompat.checkSelfPermission(WifiActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<ScanResult> wifiList = wifiManager.getScanResults();



                wifiNetworkAdapter.clear();
                wifiNetworkAdapter.addAll(wifiList);
                wifiNetworkAdapter.notifyDataSetChanged();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void connectToWifi(ScanResult wifiNetwork) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter WiFi Password");

        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordEditText);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            String password = passwordEditText.getText().toString();

            WifiNetworkSpecifier.Builder specifierBuilder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                specifierBuilder = new WifiNetworkSpecifier.Builder()
                        .setSsid(wifiNetwork.SSID)
                        .setWpa2Passphrase(password);
            }

            WifiNetworkSpecifier wifiNetworkSpecifier = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                wifiNetworkSpecifier = specifierBuilder.build();
            }

            NetworkRequest.Builder requestBuilder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                requestBuilder = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(wifiNetworkSpecifier);
            }

            ConnectivityManager connectivityManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                connectivityManager = getSystemService(ConnectivityManager.class);
            }
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    // Network is available, perform any necessary operations
                    Toast.makeText(WifiActivity.this, "Connected to WiFi network: " + wifiNetwork.SSID, Toast.LENGTH_SHORT).show();
                    // You can use the network object for further network-related operations
                }
            };

            connectivityManager.requestNetwork(requestBuilder.build(), networkCallback);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}