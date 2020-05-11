package com.example.updatedbackgroundapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    protected static final int NOTIFICATION_ID = 911;
    private static String TAG = "MainService";
    private static MainService myCurrentService;
    private static Timer timer;
    public static final long interval = 600000; //milisekundi vo 10 min
    public NetworkInfo info;
    public static final String apiUrl = "http://192.168.1.103:5000/getjobs/hardware";
    public String unParsed = "";
    public static final int JOB_ID = 123;

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
        Log.d("Msg", "On create method of main service.");
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
        if(intent == null)
        {
            ProcessMain cls = new ProcessMain();
            cls.launchService(this);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground();
        }
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

    class backgroundTask extends TimerTask
    {
        @Override
        public void run() {
            ConnectivityManager connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifi = info.isConnected();
            if(isWifi)
            {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String line = "";
                    Log.d("UH", "Succsessfully connected, initiating data fetch process.");
                    while(line!=null)
                    {
                        line = reader.readLine();
                        unParsed = unParsed + line;
                    }
                    JSONArray array = new JSONArray(unParsed);
                    JSONObject object = (JSONObject) array.get(0); // znaeme odnapred deka imame samo 1 clen vo ovaa niza to est 1 job type
                    String singleParsed="";
                    singleParsed = singleParsed + object.get("date")+"|"+object.get("host")+"|"+object.get("count")+"|"+object.get("packetSize")+"|"+object.get("jobPeriod")+"|"+object.get("jobType");
                    Log.d("UHM", singleParsed);
                    if(object.get("jobType")=="PING")
                    {
                        PersistableBundle bundle = new PersistableBundle();
                        bundle.putString("Data", singleParsed);
                        ComponentName compName = new ComponentName(getApplicationContext(), JobPing.class);
                        JobInfo info = new JobInfo.Builder(JOB_ID, compName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                                .setPersisted(true)
                                .setExtras(bundle)
                                .setOverrideDeadline(2000).build();
                        JobScheduler scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
                        int resultCode = scheduler.schedule(info);
                        if(resultCode == JobScheduler.RESULT_SUCCESS)
                        { Log.d("Job", "Job succsessfully scheduled."); }
                        else { Log.d("Job error", "Job scheduling failed.");}
                    }
                    else {Log.d("Job type error", "Error undefined job type.");}
                } catch (MalformedURLException e) {
                    Log.d("URL error", "Error: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Reader error", "Error: " + e.getMessage());
                } catch (JSONException e) {
                    Log.d("JSON error", "Error: " + e.getMessage());
                }
            }
            else { Log.d("Network error", "Error must be connected to wifi."); }
        }
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
        stopTimerTask();
        Log.d(TAG, "Starting background timer task!");
        timer = new Timer();
        Log.d(TAG, "Scheduling...");
        timer.scheduleAtFixedRate(new backgroundTask(), 0, interval);
    }

    public void stopTimerTask()
    {
        Log.d("MSG1", "Stopping existing timer task.");
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
