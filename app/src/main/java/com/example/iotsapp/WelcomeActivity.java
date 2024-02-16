package com.example.iotsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class WelcomeActivity extends AppCompatActivity {

    ProgressBar progressBar;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        if (auth.getCurrentUser() != null){
            new CountDownTimer(5000, 1000) {
                public void onFinish() {
                    startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                }
                public void onTick(long millisUntilFinished) {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(WelcomeActivity.this, "please wait you are already logged in", Toast.LENGTH_SHORT).show();
                }
            }.start();
        }
    }

    public void login(View view) {
        startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
    }

    public void registration(View view) {
        startActivity(new Intent(WelcomeActivity.this,RegistrationActivity.class));
    }
}