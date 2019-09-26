package ch.teko.weatherservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static boolean connected = false;
    private static final String LOG_TAG = "ConnectivityReceiver";

    public void init(Context context){
        Log.d(LOG_TAG, "init() called");
        connected = isNetworkInterfaceAvailable(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive() called");
        try {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                connected = isNetworkInterfaceAvailable(context);
                Log.d(LOG_TAG, "connected = " + connected);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkInterfaceAvailable(Context context) {
        Log.d(LOG_TAG, "isNetworkInterfaceAvailable() called");
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public static boolean isConnected() {
        return connected;
    }
}
