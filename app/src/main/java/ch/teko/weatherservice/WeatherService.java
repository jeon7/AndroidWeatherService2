package ch.teko.weatherservice;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {


    private static boolean serviceStarted = false;
    private Timer taskScheduler;
    private static final long TASK_DELAY = 1000;
    private static final long TASK_PERIOD = 5000;
    private static final String LOG_TAG = "WeatherService";
    private static Weather latestWeatherObj = null;

    public WeatherService() {
        Log.d(LOG_TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() called");
        super.onCreate();

        WeatherNotification.configureNotification(getApplicationContext());
        WeatherNotification.createNotificationChannel(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind() called");
        return new LocalBinder(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand() called");
        serviceStarted = true;

        setScheduledTask(new TimerTask() {
            @Override
            public void run() {
                if (!ConnectivityReceiver.isConnected()) {
                    stopService(intent);
                    this.cancel();
                    // todo notify user
                } else {
                    latestWeatherObj = WeatherAPI.fetchWeather();
                    if (latestWeatherObj == null) {
//                        todo
                        Toast.makeText(getApplicationContext(),
                                "check the Weather API website, it may not work", Toast.LENGTH_SHORT).show();
                    } else {
//                        todo
                        Toast.makeText(getApplicationContext(),
                                latestWeatherObj.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Log.d(LOG_TAG, latestWeatherObj.toString());
                }

//                    todo notification test
                if (latestWeatherObj.getTime_stamp_cet().equals("27.09.2019 20:00:00")) {
                    WeatherNotification.runNotification(getApplicationContext());
                }

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        taskScheduler.cancel();
        serviceStarted = false;
        super.onDestroy();
    }

    public void setScheduledTask(TimerTask scheduledTask) {
        taskScheduler = new Timer();
        taskScheduler.scheduleAtFixedRate(scheduledTask, TASK_DELAY, TASK_PERIOD);
    }

    public static boolean isServiceStarted() {
        return serviceStarted;
    }

    public class LocalBinder extends Binder {
        private WeatherService service;
        private static final String LOG_TAG = "LocalBinder";

        public LocalBinder(WeatherService service) {
            Log.d(LOG_TAG, "constructor called, parameter service saved to LocalBinder field service");
            this.service = service;
        }

        public WeatherService getService() {
            Log.d(LOG_TAG, "getService() called");
            return service;
        }
    }
}
