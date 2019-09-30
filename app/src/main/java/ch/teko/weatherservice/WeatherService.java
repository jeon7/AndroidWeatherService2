package ch.teko.weatherservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {

    private static final String LOG_TAG = "WeatherService";
    private static boolean serviceStarted = false;
    private Handler handler;
    private Timer taskScheduler;
    private static final long TASK_DELAY = 1000;
    private static final long TASK_PERIOD = 5000;
    private Weather latestWeatherObj = null;

    public WeatherService() {
        Log.d(LOG_TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() called");
        handler = new Handler();
        WeatherNotification.configureNotification(getApplicationContext());
        WeatherNotification.createNotificationChannel(getApplicationContext());
        super.onCreate();
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
            serviceStarted = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.setUI();
                }
            });
            processScheduledTask(intent);
            return super.onStartCommand(intent, flags, startId);
        }
    }

    private void processScheduledTask(Intent intent) {

        String tempDiff = intent.getStringExtra("main_temp_diff");
        Log.d(LOG_TAG, "tempDiff = " + tempDiff);


        setScheduledTask(new TimerTask() {
            @Override
            public void run() {
                if (!isServiceStarted()) {
                    this.cancel();
                }
                if (!ConnectivityReceiver.isConnected()) {
                    this.cancel();
                    stopSelf();
                    // todo notify user
                } else {
                    latestWeatherObj = WeatherAPI.fetchWeather();
                    if (latestWeatherObj == null) {
                        Log.d(LOG_TAG, "check the Weather API website, it may not work at the moment");
                    } else {
                        Log.d(LOG_TAG, latestWeatherObj.toString());
//                         todo add calculation
//                        Toast.makeText(getApplicationContext(),
//                                latestWeatherObj.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
//                    todo notification test
                if (latestWeatherObj.getTime_stamp_cet().equals("27.09.2019 20:00:00")) {
                    WeatherNotification.runNotification(getApplicationContext());
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        taskScheduler.cancel();
        serviceStarted = false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUI();
            }
        });
        super.onDestroy();
    }

    public void setScheduledTask(TimerTask scheduledTask) {
        taskScheduler = new Timer();
        taskScheduler.scheduleAtFixedRate(scheduledTask, TASK_DELAY, TASK_PERIOD);
    }

    public static boolean isServiceStarted() {
        return serviceStarted;
    }
}
