package com.example.backgroundapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    Intent serviceIntent;
    private SensotService mySensotService;
    Context ctx;

    public Context getCtx()
    {
        return ctx;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_main);
        ctx = this;
        mySensotService = new SensotService(getCtx());
        serviceIntent = new Intent(getCtx(), mySensotService.getClass());
        Log.d("hehe", "OnCreate method initiated!");
        if(!isServiceRunning(mySensotService.getClass()))
        {
            startService(serviceIntent);
            Log.d("huehue", "Command to start service has been sent!");
        }
        finish();
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                Log.d("Checking service status", " Service is up!");
                return true;
            }

        }
        Log.d("Checking service status", "Service is down!");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        Log.d("Service destroy", "Service stopped via main activity!");
        super.onDestroy();
    }
}
