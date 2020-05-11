package com.example.updatedbackgroundapplication;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    public static final int JOB_ID = 1;
    public static final String TAG = RestartServiceBroadcastReceiver.class.getSimpleName();
    private static JobScheduler jobScheduler;
    private RestartServiceBroadcastReceiver restartSensorServiceReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "about to start timer " + context.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        { scheduleJob(context); }
        else {
            registerRestarterReceiver(context);
            ProcessMain cls = new ProcessMain();
            cls.launchService(context);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context)
    {
        Log.d("randomTag", "scheduleJob of RestartServiceBroadcastReceiver called!");
        if (jobScheduler == null)
        { jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            Log.d("afterTag", "New job scheduler instance made!");
        }
        ComponentName compName = new ComponentName(context, JobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, compName)
                .setOverrideDeadline(0)
                .setPersisted(true).build();
        jobScheduler.schedule(jobInfo);
    }

    private void registerRestarterReceiver(final Context context)
    {
        Log.d("tat", "Register restarter receiver called!");
        if (restartSensorServiceReceiver == null)
        {   restartSensorServiceReceiver = new RestartServiceBroadcastReceiver();
            Log.d("tatr", "New isntance of restartSensor made!");
        }
        else try
        {
            context.unregisterReceiver(restartSensorServiceReceiver);
            Log.d("tats", "Unregistered receiver!");
        }
        catch (Exception e)
        {

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IntentFilter filter = new IntentFilter();
                filter.addAction(GlobalClass.RESTART_INTENT);
                try {
                    context.registerReceiver(restartSensorServiceReceiver, filter);
                } catch (Exception e) {
                    try {
                        context.getApplicationContext().registerReceiver(restartSensorServiceReceiver, filter);
                    } catch (Exception ex) {

                    }
                }
            }
        }, 1000);
    }
}
