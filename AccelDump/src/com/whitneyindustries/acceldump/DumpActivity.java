package com.whitneyindustries.acceldump;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class DumpActivity extends Activity
{
    private static final String TAG = DumpActivity.class.getSimpleName();

    private StringBuilder mDisplayText = new StringBuilder();

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DataPostReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                     10000,
                                     AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                                     alarmIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
            alarmMgr = null;
        }
        super.onSaveInstanceState(outState);
    }


    private void updateUi() {
        TextView textView = (TextView) findViewById(R.id.log_data_text_view);
        textView.setText(mDisplayText.toString());
    }

}
