package com.whitneyindustries.acceldump;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DumpActivity extends Activity
{
    private static final String TAG = DumpActivity.class.getSimpleName();

    private StringBuilder mDisplayText = new StringBuilder();

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private EditText mIpField;
    private Button mIpSave;

    private OnClickListener mIpSaveListener = new OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(getString(R.string.server_ip), mIpField.getText().toString());
                editor.commit();
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String ip = getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.server_ip), "127.0.0.1");
        mIpField = (EditText)findViewById(R.id.ip_field);
        mIpField.setText(ip);
        mIpSave = (Button)findViewById(R.id.ip_save);
        mIpSave.setOnClickListener(mIpSaveListener);


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
        //if (alarmMgr != null) {
        //    alarmMgr.cancel(alarmIntent);
        //    alarmMgr = null;
        //}
        super.onSaveInstanceState(outState);
    }


    private void updateUi() {
        TextView textView = (TextView) findViewById(R.id.log_data_text_view);
        textView.setText(mDisplayText.toString());
    }

}
