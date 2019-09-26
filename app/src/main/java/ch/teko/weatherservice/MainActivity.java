package ch.teko.weatherservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public TextView tv_status_internet;
    private TextView tv_status_service;
    private TextView tv_note;
    private EditText et_temp_diff;

    private WeatherService service;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private ConnectivityReceiver connectivityReceiver;

    private String tempDiff;
    private String tempLastChecked;
    private String timeLastChecked;
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, WeatherService.class);
        serviceConnection = new WeatherServiceConnection();

        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter receiverFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, receiverFilter);

        tv_status_internet = findViewById(R.id.tv_status_internet);
        tv_status_service = findViewById(R.id.tv_status_service);
        tv_note = findViewById(R.id.tv_note);
        et_temp_diff = findViewById(R.id.editText_temperature_diff);

        Button btnStartService = findViewById(R.id.button_start_service);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(serviceIntent);
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

                tempDiff = et_temp_diff.getText().toString();
//                todo: test
                tv_status_service.setText(tempDiff);

//                todo: test get json: make a class. this will run in a thread on service
                Thread weatherThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Weather latestWeatherObj = WeatherAPI.fetchWeather();
                        Log.d(LOG_TAG, latestWeatherObj.toString());
                    }
                });
                weatherThread.start();

            }
        });

        Button btnEndService = findViewById(R.id.button_end_service);
        btnEndService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (service.isServiceBound()) {
                    unbindService(serviceConnection);
                }
                stopService(serviceIntent);
            }
        });



        // hide keyboard when not focused on editText
        et_temp_diff.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart() called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume() called");
        super.onResume();

        if (ConnectivityReceiver.isConnected()) {
            tv_status_internet.setText("connected to the Internet");
        } else {
            tv_status_internet.setText("not connected to the Internet");
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause() called");
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop() called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();

//        service ends when Activity-onDestroy() called
//        connectivityReceiver doesn't receive broadcast message after Activity-onDestroy().
        if (service.isServiceBound()) {
            unbindService(serviceConnection);
        }
        stopService(serviceIntent);
        unregisterReceiver(connectivityReceiver);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public class WeatherServiceConnection implements ServiceConnection {

        private static final String LOG_TAG = "WeatherServiceConn";

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected() called");
            service = ((WeatherService.LocalBinder) iBinder).getService();
        }

        // todo: never called, why?
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "onServiceDisconnected() called");
            service = null;
        }
    }
}
