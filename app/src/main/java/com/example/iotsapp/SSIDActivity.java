package com.example.iotsapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;

import android.net.wifi.WifiManager;
import android.content.Context;

import android.Manifest;

public class SSIDActivity extends AppCompatActivity {

    private TextView ssidText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssid);

        ssidText = findViewById(R.id.ssidText);
        String currentSSID = getCurrentSSID(); // Get and display the current SSID

        if (currentSSID != null) {
            ssidText.setText(currentSSID);
        } else {
            ssidText.setText("No WiFi Connection");
        }

        Button selectSSIDButton = findViewById(R.id.select_ssid_button);
        selectSSIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSSID != null) {
                    Intent returnIntent = new Intent();
                    Toast.makeText(getApplicationContext(), "SSID set to " + currentSSID, Toast.LENGTH_LONG).show();
                    returnIntent.putExtra("selectedSSID", currentSSID);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "No WiFi Connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getCurrentSSID() {
        String ssid;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = wifiInfo.getSSID();
            ssid = ssid.replaceAll("^\"|\"$", "");
            return ssid;
        }
        return null;
    }
}
