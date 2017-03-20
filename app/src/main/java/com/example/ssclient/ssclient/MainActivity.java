package com.example.ssclient.ssclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String RECEIVE_DATA = "com.example.ssclient.ssclient.action.SS_STATUS";

    private BroadcastReceiver bReceiver;
    private ImageButton imgBtn;
    private LocalBroadcastManager bManager;
    private TextView mText;
    private TextView mTapToActivate;
    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.textView);
        mTapToActivate = (TextView) findViewById(R.id.twTapToActivate);

        mStatus = (TextView) findViewById(R.id.txtStatus);

        imgBtn = (ImageButton) findViewById(R.id.imageButton);
        imgBtn.setBackgroundResource(R.drawable.inactive_circle);

        setupUI();

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                NetService.soSetRemoteStatus(MainActivity.this, "", "");
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you want to set status to enabled?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
            }
        });

        NetService.doCheckStatus(MainActivity.this, "", "");

        // preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((TextView) findViewById(R.id.txtIPaddr)).setText(prefs.getString("ipAddress", "0.0.0.0"));
        ((TextView) findViewById(R.id.txtPort)).setText(prefs.getString("portNum", "0"));

        // intent handler
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(RECEIVE_DATA)) {
                    String data = intent.getStringExtra("data");
                    HandleNetworkData(data);
                }
            }
        };
        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_DATA);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    private void setupUI() {
        imgBtn.setBackgroundResource(R.drawable.inactive_circle);
        mText.setText( "INACTIVE" );
        mText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.inactiveColor));
        mTapToActivate.setVisibility(View.VISIBLE);

        mStatus.setText("connecting..");
        mStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.inactiveColor));
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupUI();

        NetService.doCheckStatus(MainActivity.this, "", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bManager.unregisterReceiver(bReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void HandleNetworkData(String data) {
        if ("1".equals(data)) {
            imgBtn.setBackgroundResource(R.drawable.green_circle);
            mText.setText( "ACTIVE" );
            mText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.fgColor));
            mTapToActivate.setVisibility(View.INVISIBLE);
            mStatus.setText("connected");
            mStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorTextSuccess));
        } else {
            imgBtn.setBackgroundResource(R.drawable.inactive_circle);
            mText.setText( "INACTIVE" );
            mText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.inactiveColor));
            mTapToActivate.setVisibility(View.VISIBLE);

            if(data.isEmpty()) {
                mStatus.setText("connection failed");
                mStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorTextError));
            } else {
                mStatus.setText("connected");
                mStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorTextSuccess));
            }
        }

        // TODO: addd extended error message
    }
}
