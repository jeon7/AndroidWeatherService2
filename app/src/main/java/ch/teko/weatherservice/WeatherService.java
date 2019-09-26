package ch.teko.weatherservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {

    private boolean serviceOn = false;
    private boolean serviceBound = false;
    private static final String LOG_TAG = "WeatherService";

    public WeatherService() {
        Log.d(LOG_TAG, "constructor called, new WeatherThread created");
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
        serviceOn = true;

        //todo: test
        TimerTask scheduledTask = new TimerTask() {
            @Override
            public void run() {
                // todo
            }
        };
        Timer taskScheduler = new Timer();
        taskScheduler.scheduleAtFixedRate(scheduledTask, 1000, 3000);
        try {
            Thread.sleep(20000);
        } catch(InterruptedException ex) {
            //
        }
        taskScheduler.cancel();


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();
        serviceOn = false;
        serviceBound = false;
    }

    public boolean isServiceOn() {
        return serviceOn;
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
