package ch.teko.weatherservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.os.Handler;

public class ConnectivityReceiver extends BroadcastReceiver {

    private final Handler handler;
    private static boolean connected = false;
    private static final String LOG_TAG = "ConnectivityReceiver";

    public ConnectivityReceiver(Handler handler) {
        this.handler = handler;
    }

//    public void init(Context context){
//        Log.d(LOG_TAG, "init() called");
//        connected = isNetworkInterfaceAvailable(context);
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive() called");
        try {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                connected = isNetworkInterfaceAvailable(context);
                Log.d(LOG_TAG, "connected = " + connected);

                if (connected == true) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.tv_status_internet.setText("Connected to Internet");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.tv_status_internet.setText("No Internet");
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
