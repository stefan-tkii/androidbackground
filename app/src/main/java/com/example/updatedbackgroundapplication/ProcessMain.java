package com.example.updatedbackgroundapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ProcessMain {
    public static final String TAG = ProcessMain.class.getSimpleName();
    private static Intent serviceIntent = null;

    public ProcessMain() {

    }

    private void setServiceIntent(Context context)
    {
        if(serviceIntent == null)
        {
            serviceIntent = new Intent(context, MainService.class);
        }
    }

    public void launchService(Context context)
    {
        if(context == null)
        {
            return;
        }
        setServiceIntent(context);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
        {
            context.startForegroundService(serviceIntent);
        }
        else
        {
            context.startService(serviceIntent);
        }
        Log.d(TAG, "Service start command initiated!");
    }


}
