package com.example.updatedbackgroundapplication;

import android.app.job.JobParameters;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobService extends android.app.job.JobService
{
    private static String TAG= JobService.class.getSimpleName();
    private static RestartServiceBroadcastReceiver restartSensorServiceReceiver;
    private static JobService instance;
    private static JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "OnStartJob called!");
        ProcessMain cls = new ProcessMain();
        cls.launchService(this);
        registerRestarterReceiver();
        instance = this;
        JobService.jobParameters = jobParameters;
        return false;
    }

    private void registerRestarterReceiver()
    {
        Log.d(TAG, "registerRestarterReceiver called!");
        if (restartSensorServiceReceiver == null)
        {  restartSensorServiceReceiver = new RestartServiceBroadcastReceiver();
            Log.d(TAG, "New instance!");
        }
        else try
        {
            unregisterReceiver(restartSensorServiceReceiver);
            Log.d(TAG, "Unregistering receiver!");
        }
        catch (Exception e) { }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // we register the  receiver that will restart the background service if it is killed
                // see onDestroy of Service
                IntentFilter filter = new IntentFilter();
                filter.addAction(GlobalClass.RESTART_INTENT);
                try {
                    registerReceiver(restartSensorServiceReceiver, filter);
                } catch (Exception e) {
                    try {
                        getApplicationContext().registerReceiver(restartSensorServiceReceiver, filter);
                    } catch (Exception ex) {

                    }
                }
            }
        }, 1000);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "OnStopJob method called");
        Intent broadcastIntent = new Intent(GlobalClass.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(restartSensorServiceReceiver);
            }
        }, 1000);
        return false;
    }
}
