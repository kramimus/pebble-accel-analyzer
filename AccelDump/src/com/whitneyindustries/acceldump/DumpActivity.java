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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;
import java.util.TimeZone;

public class DumpActivity extends Activity
{
    private static final String TAG = DumpActivity.class.getSimpleName();

    private StringBuilder mDisplayText = new StringBuilder();

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private EditText mIpField;
    private Button mIpSave;
    private Button mSyncNow;

    private Spinner mTzSelector;
    private String selectedTz;

    private OnClickListener mIpSaveListener = new OnClickListener() {
            public void onClick(View v) {
                String ip = mIpField.getText().toString();
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(getString(R.string.server_ip), ip);
                editor.commit();
                updatePendingIntent(ip);
            }
        };

    private OnClickListener mSyncListener = new OnClickListener() {
            public void onClick(View v) {
                String ip = mIpField.getText().toString();
                Intent intent = new Intent(DumpActivity.this, DataPostService.class);
                intent.putExtra("server_ip", ip);
                intent.putExtra("tz", selectedTz);
                startService(intent);
            }
        };

    private OnItemSelectedListener mTzListener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String ip = mIpField.getText().toString();
                String selectedTz = (String)parent.getItemAtPosition(pos);
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(getString(R.string.tz), selectedTz);
                editor.commit();
                updatePendingIntent(ip);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String ip = getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.server_ip), "127.0.0.1");
        mIpField = (EditText)findViewById(R.id.ip_field);
        mIpField.setText(ip);
        mIpSave = (Button)findViewById(R.id.ip_save);
        mIpSave.setOnClickListener(mIpSaveListener);

        mSyncNow = (Button)findViewById(R.id.sync_now);
        mSyncNow.setOnClickListener(mSyncListener);

        selectedTz = getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.tz),
                                                                    "America/New_York");
        Log.d(TAG, selectedTz);

        updatePendingIntent(ip);

        mTzSelector = (Spinner)findViewById(R.id.tz_selector);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                                                             R.array.tz_array,
                                                                             android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTzSelector.setOnItemSelectedListener(mTzListener);
        mTzSelector.setAdapter(adapter);
        int position = adapter.getPosition(selectedTz);
        mTzSelector.setSelection(position);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        //if (alarmMgr != null) {
        //    alarmMgr.cancel(alarmIntent);
        //    alarmMgr = null;
        //}
        super.onSaveInstanceState(outState);
    }

    private void updatePendingIntent(String ip) {
        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DataPostReceiver.class);
        intent.putExtra("server_ip", ip);
        intent.putExtra("tz", selectedTz);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                     10000,
                                     AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                                     alarmIntent);
    }

    private void updateUi() {
        TextView textView = (TextView) findViewById(R.id.log_data_text_view);
        textView.setText(mDisplayText.toString());
    }

}
