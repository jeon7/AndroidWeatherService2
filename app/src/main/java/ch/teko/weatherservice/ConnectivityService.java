package ch.teko.weatherservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ConnectivityService extends Service {

    private static final String LOG_TAG = "ConnectivityService";
    private ConnectivityReceiver connectivityReceiver;
    private static final String NOTIFICATION_CHANNEL_ID = "ConnectivityServiceChannel";

    public ConnectivityService() {
        Log.d(LOG_TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() called");
        super.onCreate();

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        android.app.Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Connectivity Service")
                .setContentText("is currently running")
                .setSmallIcon(R.drawable.android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        connectivityReceiver = new ConnectivityReceiver(new Handler());
        IntentFilter receiverFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, receiverFilter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Weather Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        unregisterReceiver(connectivityReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
