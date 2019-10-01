package ch.teko.weatherservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.os.Handler;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ConnectivityReceiver";
    private static boolean connected = false;
    private final Handler handler;

    public ConnectivityReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive() called");
        try {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                connected = isNetworkInterfaceAvailable(context);
                Log.d(LOG_TAG, "connected = " + connected);

                if (MainActivity.isActivityRunningForeGround()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.setUI();
                        }
                    });
                }
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
