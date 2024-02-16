package com.example.iotsapp;


import android.content.SharedPreferences;
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

import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;

import android.net.wifi.WifiManager;
import android.content.Context;

import android.Manifest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {
    private void showBiometricPrompt () {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setDescription("Please authenticate with your biometrics to continue")
                .setDeviceCredentialAllowed(true)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), "Authentication successful", Toast.LENGTH_SHORT).show();
                        sendDataToESP8266("trigger_motor");
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }
    private static final int LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
        } else {
            loadSSIDFromSharedPreferences(); // Load the SSID at startup
            setWifiIndicator();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, do the location-related task
                setWifiIndicator();
            }
        }
    }

    Button authenticateButton;
    Button SSID;

    private static final int SSID_REQUEST = 2;
    private String selectedSSID;

    private void startSSIDActivity() {
        Intent intent = new Intent(this, SSIDActivity.class);
        startActivityForResult(intent, SSID_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SSID_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedSSID = data.getStringExtra("selectedSSID");
                // Use the selected SSID as needed
                saveSSIDToSharedPreferences(selectedSSID); // Save the new SSID
            }
        }
    }

    private void saveSSIDToSharedPreferences(String ssid) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SSID", ssid);
        editor.apply();
    }

    private void loadSSIDFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        selectedSSID = sharedPreferences.getString("SSID", ""); // Default value if not found is empty string
    }



    private void setWifiIndicator() {

        authenticateButton = (Button) findViewById(R.id.authenticateButton);
        SSID = (Button) findViewById(R.id.ssidButton);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            String ssid = wifiInfo.getSSID();
        }


        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String connectedSSID = wifiInfo.getSSID();
                // Add double quotes around the selected SSID
                String expectedSSID = "\"" + selectedSSID + "\"";

                if (connectedSSID.equals(expectedSSID)) {
                    showBiometricPrompt();
                } else {
                    Toast.makeText(getApplicationContext(), "Connect to the correct WiFi. Current WiFi is " + expectedSSID, Toast.LENGTH_SHORT).show();
                }
            }
        });

        SSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), wifiInfo.getSSID(), Toast.LENGTH_LONG).show();
                startSSIDActivity();
            }
        });
    }

    public void sendDataToESP8266(final String value) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = "http://172.20.10.12/data?value=" + URLEncoder.encode(value, "UTF-8");
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        Log.d("NetworkRequest", "Response: " + result.toString());
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("NetworkRequest", "Error: " + e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

}




