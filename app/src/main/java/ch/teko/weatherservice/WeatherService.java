package ch.teko.weatherservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {

    private static boolean serviceBound = false;
    private Timer taskScheduler;
    private static final long TASK_DELAY = 1000;
    private static final long TASK_PERIOD = 5000;
    private static final String LOG_TAG = "WeatherService";

    public WeatherService() {
        Log.d(LOG_TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() called");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind() called");
        serviceBound = true;
        return new LocalBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand() called");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        taskScheduler.cancel();
        serviceBound = false;
        super.onDestroy();
    }

    public void setScheduledTask (TimerTask scheduledTask) {
        taskScheduler = new Timer();
        taskScheduler.scheduleAtFixedRate(scheduledTask, TASK_DELAY, TASK_PERIOD);
    }

    public boolean isServiceBound() {
        return serviceBound;
    }

    public static class LocalBinder extends Binder {
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
