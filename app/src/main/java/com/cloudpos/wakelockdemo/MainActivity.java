package com.cloudpos.wakelockdemo;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initParams();
    }

    private void initParams() {
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    private void initUI() {
        Button mSleep = findViewById(R.id.keepWorking);
        mSleep.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keepWorking:
                keepWorking();
                break;
        }
    }

    public void keepWorking() {
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.i("MainActivity", "Print Log : " + System.currentTimeMillis());
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

}