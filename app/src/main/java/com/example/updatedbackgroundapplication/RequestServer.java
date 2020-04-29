package com.example.updatedbackgroundapplication;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;

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

public class RequestServer extends AsyncTask<Void, Void, Void> {

    public Handler mHandler;
    public int interval = 600000; //broj na milisekundi vo 10 minuti
    public static final String apiUrl = "http://localhost:5000/getjobs";
    public ConnectivityManager connManager;
    public String unParsed = "";
    public String singleParsed="";
    public NetworkInfo info;
    public Context mContext;
    public static final int JOB_ID = 2;

    public RequestServer(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    protected Void doInBackground(Void... voids)
    {
        startRepeatingBackground();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    Runnable backgroundJob = new Runnable() {
        @Override
        public void run() {
            try
            {
                connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                        while(line!=null)
                        {
                            line = reader.readLine();
                            unParsed = unParsed + line;
                        }
                        JSONArray array = new JSONArray(unParsed);
                        JSONObject object = (JSONObject) array.get(0); //imame samo eden element
                        singleParsed = singleParsed + object.get("date")+"|"+object.get("host")+"|"+object.get("count")+"|"+object.get("packetSize")+"|"+object.get("jobPeriod")+"|"+object.get("jobType");
                        if(object.get("jobType")=="PING")
                        {
                            PersistableBundle bundle = new PersistableBundle();
                            bundle.putString("Data", singleParsed);
                            ComponentName compName = new ComponentName(mContext, JobPing.class);
                            JobInfo info = new JobInfo.Builder(JOB_ID, compName)
                                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                                    .setPersisted(true)
                                    .setExtras(bundle)
                                    .setOverrideDeadline(5000).build();
                            JobScheduler scheduler = (JobScheduler)mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                            int resultCode = scheduler.schedule(info);
                            if(resultCode == JobScheduler.RESULT_SUCCESS)
                            {
                                Log.d("Job", "Job succsessfully scheduled.");
                            }
                            else
                            {
                                Log.e("Job error", "Job scheduling failed.");
                            }
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.e("NETWORK ERROR", "Error cannot execute task, no internet connection.");
                }

            }
            finally {
                mHandler.postDelayed(backgroundJob, interval);
            }
        }
    };

    public void startRepeatingBackground()
    {
        backgroundJob.run();
    }
}






