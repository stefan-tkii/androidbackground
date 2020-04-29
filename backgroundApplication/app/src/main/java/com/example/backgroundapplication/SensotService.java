package com.example.backgroundapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class SensotService extends Service {
    public int counter = 0;
    private Timer timer;
    private TimerTask task;
    long oldTime = 0;

    public SensotService(Context appContext) {
        super();
        Log.d("Service constructor", "Service has been created!");
    }

    public SensotService() {}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        SharedPreferences prefs = this.getSharedPreferences("com.example.backgroundapplication.SensotService", MODE_PRIVATE);
        counter = prefs.getInt("counter", 0);
        startTimer();
        Log.d("tag4", "onStartCommand method initiated!");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("tag3", "Bind function initiated!");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try
        {
            SharedPreferences pref = getSharedPreferences("com.example.backgroundapplication.SensotService", MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            edit.putInt("counter", counter);
            edit.apply();
            Log.d("saveMode", "Saving counter value before destroying!");
        }
        catch (NullPointerException e)
        {
            Log.e("tagerror", "Error saving: "+ e.getMessage());
        }
        Log.d("Destroy method", "Service destroyed");
        Intent newIntent = new Intent(this, ServiceRestarter.class);
        sendBroadcast(newIntent);
        stopTimer();
    }

    public void InitializeTimerTask()
    {
        task = new TimerTask() {
            @Override
            public void run() {
                Log.d("in timer", "in timer + " + (counter++));
            }
        };
    }

    public void startTimer()
    {
        Log.d("Start", "Timer is starting!");
        timer = new Timer();
        InitializeTimerTask();
        timer.schedule(task, 1000, 1000); // sekoja 1 sekunda
    }

    public void stopTimer()
    {
        if(timer!=null)
        {
            Log.d("tag1", "Timer is stopped!");
            timer.cancel();
            timer = null;
        }
        Log.d("tag2", "Timer task in, but timer already stopped!");
    }


}
