package com.example.iotsapp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.view.WindowManager;
import androidx.constraintlayout.motion.widget.MotionLayout;


public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();

        //MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.amongus);
        //mediaPlayer.start();

        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                        /*imageView.findViewById(R.id.splashPic);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);

                        imageView.startAnimation(animation);

                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart (Animation animation) {

                            }
                            @Override
                            public void onAnimationEnd (Animation animation) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                                mediaPlayer.stop();
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });*/
                        finish();
                    }
                }, 3000);
    }
}