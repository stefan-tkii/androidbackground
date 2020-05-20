package com.example.updatedbackgroundapplication;

import android.app.Service;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class JobPing extends JobService {
    public static final String result1 = "result1";
    public static final String result2 = "result2";
    public static final String result3 = "result3";
    public static final String result = "result";
    public static final String postUrl = "http://192.168.1.104:5000/postresults";
    public static final String preferenceID = "mySavedData";
    public SharedPreferences preferences;
    public NetworkInfo info;
    public ConnectivityManager manager;
    public String inputString="";
    public JobPing() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        String[] data = params.getExtras().getString("Data").split("|");
        String pingCommand = "ping -s " + data[3] + " -c " + data[2]+ " -i " + data[4] + " " + data[1];
        Runtime r = Runtime.getRuntime();
        try
        {
            Process p = r.exec(pingCommand);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line="";
            while((line = in.readLine())!=null)
            {
                inputString = inputString + line;
            }
            in.close();
            manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifi = info.isConnected();
            if(isWifi)
            {
                backgroundRunnable runnable = new backgroundRunnable(inputString, preferenceID);
                new Thread(runnable).start();
            }
            else
            {
                preferences = getSharedPreferences(preferenceID, MODE_PRIVATE);
                String res1 = preferences.getString(result1, "");
                String res2 = preferences.getString(result2, "");
                String res3 = preferences.getString(result3, "");
                if(res1!="" && res2!="" && res3!="")
                {
                    preferences.edit().remove(result1).apply();
                    preferences.edit().remove(result2).apply();
                    preferences.edit().remove(result3).apply();
                    preferences.edit().putString(result1, res2).apply();
                    preferences.edit().putString(result2, res3).apply();
                    preferences.edit().putString(result3, inputString).apply();
                }
                else if(res1!="" && res2!="")
                {
                    preferences.edit().putString(result3, inputString).apply();
                }
                else if(res1!="")
                {
                    preferences.edit().putString(result2, inputString).apply();
                }
                else
                {
                    preferences.edit().putString(result1, inputString).apply();
                }
            }
        }
        catch (IOException e)
        {
            Log.d("IOerr", "Error: " + e.getMessage());
        }
        return true;
    }


    class backgroundRunnable implements Runnable
    {
      public String preferenceID;
      public String initialResult;

      backgroundRunnable(String initialResult, String preferenceID)
      {
          this.initialResult = initialResult;
          this.preferenceID = preferenceID;
      }

        @Override
        public void run()
        {
            SharedPreferences preferences = getSharedPreferences(preferenceID, MODE_PRIVATE);
            try
            {
                URL url = new URL(postUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                String res1 = preferences.getString(result1, "");
                String res2 = preferences.getString(result2, "");
                String res3 = preferences.getString(result3, "");
                if(res1!="" && res2!="" && res3!= "")
                {
                    JSONObject jsonparam = new JSONObject();
                    jsonparam.put(result, res1 + ";" + res2 + ";" + res3);
                    DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(jsonparam.toString(), "UTF-8"));
                    printout.flush();
                    printout.close();
                    preferences.edit().remove(result1).apply();
                    preferences.edit().remove(result2).apply();
                    preferences.edit().remove(result3).apply();
                    preferences.edit().putString(result1, initialResult);
                }
                else if(res1!="" && res2!="")
                {
                    JSONObject jsonparam = new JSONObject();
                    jsonparam.put(result, res1 + ";" + res2 + ";" + initialResult);
                    DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(jsonparam.toString(), "UTF-8"));
                    printout.flush();
                    printout.close();
                    preferences.edit().remove(result1).apply();
                    preferences.edit().remove(result2).apply();
                }
                else if(res1!="")
                {
                    JSONObject jsonparam = new JSONObject();
                    jsonparam.put(result, res1 + ";" + initialResult);
                    DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(jsonparam.toString(), "UTF-8"));
                    printout.flush();
                    printout.close();
                    preferences.edit().remove(result1).apply();
                }
                else
                {
                    JSONObject jsonparam = new JSONObject();
                    jsonparam.put(result, initialResult);
                    DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(jsonparam.toString(), "UTF-8"));
                    printout.flush();
                    printout.close();
                }
                try
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while( (responseLine = br.readLine()) != null )
                    {
                        response.append(responseLine.trim());
                    }
                    Log.d("Result", "Result is: " + response.toString());
                }
                catch (IOException e)
                {
                    Log.d("ResultERR", "Error: " + e.getMessage());
                }
            }
            catch (MalformedURLException e)
            {
                Log.d("URLerror", "Error: " + e.getMessage());
            }
            catch (IOException e)
            {
                Log.d("IOerror", "Error: " + e.getMessage());
            }
            catch (JSONException e)
            {
                Log.d("JSONerror", "Error: " + e.getMessage());
            }
        }
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return super.onStopJob(params);
    }
}
