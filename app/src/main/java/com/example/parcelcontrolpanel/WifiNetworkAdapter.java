package com.example.parcelcontrolpanel;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class WifiNetworkAdapter extends ArrayAdapter<ScanResult> {

    private LayoutInflater inflater;

    public WifiNetworkAdapter(Context context, List<ScanResult> wifiList) {
        super(context, R.layout.list_item_wifi_network, wifiList);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_wifi_network, parent, false);
        }

        TextView wifiNameTextView = convertView.findViewById(R.id.wifiNameTextView);
        TextView wifiSignalTextView = convertView.findViewById(R.id.wifiSignalTextView);

        ScanResult wifiNetwork = getItem(position);

        wifiNameTextView.setText(wifiNetwork.SSID);
        wifiSignalTextView.setText(String.valueOf(wifiNetwork.level));

        return convertView;
    }
}