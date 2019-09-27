package ch.teko.weatherservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WeatherNotification {

    private static final String NOTIFICATION_CHANNEL_ID = "temp_fluctuation";
    private static final String NOTIFICATION_CONTENT_TITEL = "Temperatur Fluctuation";
    private static String NOTIFICATION_CONTENT_TEXT = "Temperatur Fluctuation over "; // todo
    private static int NOTIFICATION_ID = 100;
    private static NotificationCompat.Builder builder;
    private static final String LOG_TAG = "WeatherNotification";

    public static void configureNotification(Context context) {
        Log.d(LOG_TAG, "configureNotification() called ");
        // intent for notification tap action
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.temperature)
                .setContentTitle(NOTIFICATION_CONTENT_TITEL)
                .setContentText(NOTIFICATION_CONTENT_TEXT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("long text" + NOTIFICATION_CONTENT_TEXT + NOTIFICATION_CONTENT_TEXT + NOTIFICATION_CONTENT_TEXT))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);
    }

    // notification - set priority for android 8.0 and higher
    public static void createNotificationChannel(Context context) {
        Log.d(LOG_TAG, "createNotificationChannel() called ");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void runNotification(Context context){
        Log.d(LOG_TAG, "runNotification() called ");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
