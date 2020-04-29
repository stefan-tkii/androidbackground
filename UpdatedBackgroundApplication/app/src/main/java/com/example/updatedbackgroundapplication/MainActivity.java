package com.example.updatedbackgroundapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d("tag1", "OnCreate method initiated!");
    }

    @Override
    protected  void onResume()
    {
        Log.d("tag2", "OnResume method initiated!");
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Log.d("tag3", "Detected vesrion above/equal to Lollipop!");
            RestartServiceBroadcastReceiver.scheduleJob(getApplicationContext());
        }
        else
        {
            Log.d("tag4", "Detected version lower than Lollipop!");
            ProcessMain clas = new ProcessMain();
            clas.launchService(getApplicationContext());
        }
        finish();
    }


}
