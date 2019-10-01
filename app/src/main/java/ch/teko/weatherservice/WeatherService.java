package ch.teko.weatherservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {

    private static final String LOG_TAG = "WeatherService";
    private static boolean serviceStarted = false;
    private Handler handler;
    private Timer taskScheduler;
    private static final long TASK_DELAY = 1000;
    private static final long TASK_PERIOD = 20000; // Fetch weather api data every 60s. todo 20000 for test => should be 60000
    private Weather lastWeatherObj = null;
    private float userInputTempDiff;

    public static final String NOTIFICATION_CHANNEL_ID = "WeatherServiceChannel";
    private static final String NOTIFICATION_TITEL_TEMP_FLUCTUATION = "Temperatur Fluctuation";
    private static final String NOTIFICATION_TITEL_CONNECTIVITY  = "Weather Service - No Internet";
    private static final String NOTIFICATION_TEXT_CONNECTIVITY  = "Service Ends, click here to start again. ";

    public WeatherService() {
        Log.d(LOG_TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() called");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        android.app.Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Weather Service")
                .setContentText("is currently running")
                .setSmallIcon(R.drawable.thermometer_black)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);

        super.onCreate();
        handler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand() called");

        if(intent == null) {
            return Service.START_STICKY;
        } else {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            android.app.Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Weather Service")
                    .setContentText("is currently running")
                    .setSmallIcon(R.drawable.thermometer_black)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(2, notification);

            serviceStarted = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.setUI();
                }
            });
            processScheduledTask(intent);

            return Service.START_NOT_STICKY;
        }
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

    private void processScheduledTask(Intent intent) {

        String tempDiffUserInput_string = intent.getStringExtra("main_temp_diff");
        userInputTempDiff = Math.abs(Float.parseFloat(tempDiffUserInput_string)); // temp diff absolute value

        Log.d(LOG_TAG, "tempDiff = " + tempDiffUserInput_string);


        setScheduledTask(new TimerTask() {
            @Override
            public void run() {
                if (!isServiceStarted()) {
                    this.cancel();
                }
                if (!ConnectivityReceiver.isConnected()) {
                    this.cancel();

                    // make notification for no Internet just when user is not using this app
                    if (!MainActivity.isActivityRunningForeGround()){
                        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                0, notificationIntent, 0);

                        android.app.Notification notification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                                .setContentTitle(NOTIFICATION_TITEL_CONNECTIVITY)
                                .setContentText(NOTIFICATION_TEXT_CONNECTIVITY)
                                .setSmallIcon(R.drawable.thermometer_orange)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setOnlyAlertOnce(true)
                                .build();

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(3, notification);
                    }

                    // and then end service
                    stopSelf();

                } else {
                    try {
                        processFetchedWeatherObj();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "check the Weather API website, it may not work at the moment");
                    }
                }
            }
        });
    }

    public void setScheduledTask(TimerTask scheduledTask) {
        taskScheduler = new Timer();
        taskScheduler.scheduleAtFixedRate(scheduledTask, TASK_DELAY, TASK_PERIOD);
    }

    private void processFetchedWeatherObj() {
        if (lastWeatherObj == null) {
            lastWeatherObj = WeatherAPI.fetchWeather();
        } else {
            Weather newWeatherObj = WeatherAPI.fetchWeather();
            float lastWeatherTemp = Float.parseFloat(lastWeatherObj.getAir_temperature().trim());
            float newWeatherTemp = Float.parseFloat(newWeatherObj.getAir_temperature().trim());
            float tempDiff = (lastWeatherTemp - newWeatherTemp); // decrease: +, increase: -
            float tempDiff_round = (Math.round(tempDiff*10.0f))/10.0f;
            Log.d(LOG_TAG, "lastWeatherTemp = " + lastWeatherTemp);
            Log.d(LOG_TAG, "newWeatherTemp = " + newWeatherTemp);
            Log.d(LOG_TAG, "tempDiff = " + tempDiff_round);
            Log.d(LOG_TAG, "userInputTempDiff = " + userInputTempDiff);

            if(Math.abs(tempDiff_round) >= userInputTempDiff) {
                // make notification for temperature fluctuation
                String tempIncreaseDecrease = "";
                if (tempDiff_round > 0) {
                    tempIncreaseDecrease = "Temperature " + Math.abs(tempDiff_round) + " decreased ";
                } else {
                    tempIncreaseDecrease = "Temperature " + Math.abs(tempDiff_round) + " increased ";
                }

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, notificationIntent, 0);

                android.app.Notification notification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(NOTIFICATION_TITEL_TEMP_FLUCTUATION)
                        .setContentText(tempIncreaseDecrease)
                        .setSmallIcon(R.drawable.thermometer_orange)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(4, notification);
            }
            lastWeatherObj = newWeatherObj;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        handler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUI();
            }
        });
        taskScheduler.cancel();
        lastWeatherObj = null;
        serviceStarted = false;
        super.onDestroy();
    }

    public static boolean isServiceStarted() {
        return serviceStarted;
    }
}
