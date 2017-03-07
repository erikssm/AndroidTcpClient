package com.example.ssclient.ssclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class NetService extends IntentService {
    private static final String ACTION_SET_STATUS = "com.example.ssclient.ssclient.action.STATUS_SET";
    private static final String ACTION_GET_STATUS = "com.example.ssclient.ssclient.action.STATUS_GET";

    public NetService() {
        super("NetService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void soSetRemoteStatus(Context context, String param1, String param2) {
        Intent intent = new Intent(context, NetService.class);
        intent.setAction(ACTION_SET_STATUS);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void doCheckStatus(Context context, String param1, String param2) {
        Intent intent = new Intent(context, NetService.class);
        intent.setAction(ACTION_GET_STATUS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SET_STATUS.equals(action)) {
                setRemotState("", "");
            } else if (ACTION_GET_STATUS.equals(action)) {
                getCurrentStatus("", "");
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void setRemotState(String param1, String param2) {
        String data = "";
        try {
            data = sendMessage("setstate");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // send intent
        Intent intent = new Intent(MainActivity.RECEIVE_DATA);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void getCurrentStatus(String param1, String param2) {
        String data = "";
        try {
            data = sendMessage("0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // send intent
        Intent intent = new Intent(MainActivity.RECEIVE_DATA);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String sendMessage(String msg) throws Exception {
        Log.d("NetService", "trying to connect socket");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Socket socket = new  Socket(InetAddress.getByName(
                sharedPref.getString("ipAddress", "127.0.0.1")),
                Integer.parseInt(sharedPref.getString("portNum", "0"))
        );

        Log.d("NetService", "writing..");
        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
        osw.write(msg.toCharArray());
        osw.flush();

        // TODO: set read timeout
        Log.d("NetService", "reading..");
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        char input[] = new char[1];
        isr.read(input);
        socket.close();

        Log.d("NetService", "received data: '" + new String(input) + "'");

        return new String(input);
    }
}
