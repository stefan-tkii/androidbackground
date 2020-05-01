package com.example.updatedbackgroundapplication;

import android.app.Service;
import android.app.job.JobParameters;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JobPing extends JobService {
    public static final String TAG = "JobPing result";

    public JobPing() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        String[] data = params.getExtras().getString("Data").split("|");
        String pingCommand = "ping -s " + data[3] + " -c " + data[2]+ " -i " + data[4] + " " + data[1];
        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec(pingCommand);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = in.readLine())!=null)
            {
                Log.d(TAG, line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return super.onStopJob(params);
    }
}
