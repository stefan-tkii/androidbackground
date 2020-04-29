package com.example.updatedbackgroundapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    protected static final int NOTIFICATION_ID = 911;
    private static String TAG = "MainService";
    private static MainService myCurrentService;
    private int counter = 0;
    private static Timer timer;
    private static TimerTask timerTask;
    long oldTime = 0;

    public MainService() {
        super();
    }

    public static Service getMyCurrentService()
    {
        return myCurrentService;
    }

    public static void setMyCurrentService(MainService currentService)
    {
        myCurrentService = currentService;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartForeground();
        }
        myCurrentService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Restarting service");
        counter = 0;
        if(intent == null)
        {
            ProcessMain cls = new ProcessMain();
            cls.launchService(this);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground();
        }
        RequestServer repeatingService = new RequestServer(this);
        repeatingService.execute();
        startTimer();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved called");
        Intent broadcastIntent = new Intent(GlobalClass.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
    }

    public void restartForeground()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Log.d(TAG, "Restarting foreground service!");
            try
            {
                Notification.Builder builder = new Notification.Builder(this)
                        .setContentTitle("MainService notification")
                        .setContentText("This is some random text!")
                        .setSmallIcon(R.drawable.ic_zz);
                Notification notification = builder.build();
                startForeground(NOTIFICATION_ID, notification);
                Log.d(TAG, "Restart foreground succsessful");
                RequestServer repeatingService = new RequestServer(this);
                repeatingService.execute();
                startTimer();
            }
            catch(Exception e)
            {
                Log.e("Error tag", "An error has occured: " + e.getMessage());
            }
        }
    }

    public void startTimer()
    {
        Log.d(TAG, "Starting timer!");
        stopTimerTask();
        timer = new Timer();
        initializeTimerTask();
        Log.d(TAG, "Scheduling...");
        timer.schedule(timerTask, 1000, 1000);
    }

    public void initializeTimerTask()
    {
        Log.d(TAG, "initialising TimerTask");
        timerTask = new TimerTask() {
            public void run() {
                Log.d("in timer", "in timer + " + (counter++));
            }
        };
    }

    public void stopTimerTask()
    {
        if(timer!=null)
        {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "OnDestroy method called!");
        Intent broadcastIntent = new Intent(GlobalClass.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        stopTimerTask();
    }
}
