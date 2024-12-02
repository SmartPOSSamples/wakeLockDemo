package com.cloudpos.screenupandoff;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;
    private ISystemExtApi systemExtApi;
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


    @Override
    protected void onResume() {
        super.onResume();
        bindSystemExtService();
    }


    protected boolean bindSystemExtService() {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.wizarpos.wizarviewagentassistant",
                "com.wizarpos.wizarviewagentassistant.SystemExtApiService");
        intent.setPackage("com.wizarpos.wizarviewagentassistant");
        intent.setComponent(comp);
        boolean isSuccess = bindService(intent, this, Context.BIND_AUTO_CREATE);
        return isSuccess;
    }

    private void initUI() {
        Button mSleep = findViewById(R.id.keepAwake);
        Button wakeup = findViewById(R.id.goToSleep);
        mSleep.setOnClickListener(this);
        wakeup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keepAwake:
                keepAwake(60000);
                break;
            case R.id.goToSleep:
                goToSleep();
                break;
        }
    }

    public void keepAwake(int timeout) {
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire(timeout);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        }, timeout);
    }

    public void goToSleep() {
        try {
            boolean result = systemExtApi.setDeviceOwner(this.getPackageName(), LockReceiver.class.getName());
            if (result) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.lockNow();
//                SystemClock.sleep(2000);
                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                } else {
                    PowerManager.WakeLock screenLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, getClass().getName());
                    screenLock.acquire();
                    screenLock.release();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            systemExtApi = ISystemExtApi.Stub.asInterface(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}