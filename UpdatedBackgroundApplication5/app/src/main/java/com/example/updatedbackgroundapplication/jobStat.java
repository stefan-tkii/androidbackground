package com.example.updatedbackgroundapplication;

import android.app.job.JobParameters;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class jobStat extends JobService
{
    public static final String postUrl = "http://192.168.1.104:5000/postresults";
    public NetworkInfo info;
    public ConnectivityManager manager;

    @Override
    public boolean onStartJob(JobParameters params) {
        String[] data2 = params.getExtras().getString("Data2").split("|");
        Log.d("Bundle2 info", "Info: " + data2[0] + data2[1] + data2[2]);
        String returnString = null;
        String statResult = "";
        try
        {
            Process pstat= null;
            try {
                pstat = Runtime.getRuntime().exec("top -n 1");
            } catch (IOException e) {
                Log.d("IOErr1", "Error:" + e.getMessage());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(pstat.getInputStream()));
            String inputLine="";
            while (returnString == null || returnString.contentEquals(""))
            {
                try {
                    returnString = in.readLine();
                } catch (IOException e) {
                    Log.d("IOErr2", "Error:" + e.getMessage());
                }
            }
            statResult += returnString +",";
            while (true)
            {
                try {
                    if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    Log.d("IOErr3", "Error:" + e.getMessage());
                }
                inputLine += ";";
                statResult += inputLine;
            }
            try {
                in.close();
            } catch (IOException e) {
                Log.d("IOErr4", "Error:" + e.getMessage());
            }
            if (pstat != null)
            {
                try {
                    pstat.getOutputStream().close();
                } catch (IOException e) {
                    Log.d("IOErr5", "Error:" + e.getMessage());
                }
                try {
                    pstat.getInputStream().close();
                } catch (IOException e) {
                    Log.d("IOErr6", "Error:" + e.getMessage());
                }
                try {
                    pstat.getErrorStream().close();
                } catch (IOException e) {
                    Log.d("IOErr7", "Error:" + e.getMessage());
                }
            }
            Log.d("Result","statResult = "+statResult);
        } catch (Exception e) {
            Log.d("Exception end", "Error:" + e.getMessage());
        }
        manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifi = info.isConnected();
        
        if(isWifi) 
            {
                backgroundTask task = new backgroundTask(statResult);
                new Thread(task).start(); 
            }
        else 
            {Log.d("Network2 error", "Error no internet to send STAT job results.");}
        
        return false;
    }

    class backgroundTask implements Runnable
    {
        public String statResult;
        public backgroundTask(String statResult)
        {
            this.statResult = statResult;
        }

        @Override
        public void run() {
            try
            {
                URL url = new URL(postUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                JSONObject jsonparam = new JSONObject();
                jsonparam.put("res", statResult);
                DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
                printout.writeBytes(URLEncoder.encode(jsonparam.toString(), "UTF-8"));
                printout.flush();
                printout.close();
                try
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while( (responseLine = br.readLine()) != null )
                    { response.append(responseLine.trim()); }
                    Log.d("Result2", "Result is: " + response.toString());
                } catch (IOException e) {
                    Log.d("Inner try stat", "Error:" + e.getMessage()); // 200 OK
                }
            } catch (ProtocolException e) {
                Log.d("ProtError", "Error" + e.getMessage());
            } catch (IOException e) {
                Log.d("IOerr", "Error:" + e.getMessage());
            } catch (JSONException e) {
                Log.d("jErr", "Error:" + e.getMessage());
            }
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return super.onStopJob(params);
    }
}
