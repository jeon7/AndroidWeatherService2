package ch.teko.weatherservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static TextView tv_status_internet;
    private TextView tv_status_service;
    private TextView tv_note;
    private EditText et_temp_diff;

    private WeatherService service;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private ConnectivityReceiver connectivityReceiver;

    private static Weather latestWeatherObj = null;
    private String tempDiff;
    private String tempLastChecked;
    private String timeLastChecked;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFERENCE_TEMP_DIFF = "user_last_saved_temp_diff";

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, WeatherService.class);
        serviceConnection = new WeatherServiceConnection();

        connectivityReceiver = new ConnectivityReceiver(new Handler());
        IntentFilter receiverFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, receiverFilter);

        tv_status_internet = findViewById(R.id.tv_status_internet);
        tv_status_service = findViewById(R.id.tv_status_service);
        tv_note = findViewById(R.id.tv_note);
        et_temp_diff = findViewById(R.id.editText_temperature_diff);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String spLastValue = sharedPreferences.getString(SHARED_PREFERENCE_TEMP_DIFF, "");
        // show last user saved temperature difference
        et_temp_diff.setText(spLastValue);

        Button btnStartService = findViewById(R.id.button_start_service);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_temp_diff.clearFocus();
                if (ConnectivityReceiver.isConnected()) {
                    if (et_temp_diff.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "type temperature difference first", Toast.LENGTH_SHORT).show();
                    } else {
                        startService(serviceIntent);
                        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        updateTempDiff();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "check internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnEndService = findViewById(R.id.button_end_service);
        btnEndService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (service == null) {
                    Toast.makeText(getApplicationContext(), "Start Service first", Toast.LENGTH_SHORT).show();
                } else {
                    if (service.isServiceBound()) {
                        unbindService(serviceConnection);
                        tv_status_service.setText("Service Off");
                    } else {
                        Toast.makeText(getApplicationContext(), "Service is already Off", Toast.LENGTH_SHORT).show();
                    }
                    stopService(serviceIntent);
                    service = null;
                }
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

    private void updateTempDiff() {
        tempDiff = et_temp_diff.getText().toString();
        // if user input (temperature differences) has new value
        if (sharedPreferences.getString(SHARED_PREFERENCE_TEMP_DIFF, "") != tempDiff) {
            SharedPreferences.Editor editor;
            editor = sharedPreferences.edit();
            editor.putString(SHARED_PREFERENCE_TEMP_DIFF, tempDiff);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume() called");
        super.onResume();

//        initial status display
        if (ConnectivityReceiver.isConnected()) {
            tv_status_internet.setText("connected to Internet");
        } else {
            tv_status_internet.setText("no Internet");
        }

        if (service != null) {
            tv_status_service.setText("Service is on");
        } else {
            tv_status_service.setText("Service is off");
        }
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop() called");
//        todo: what is voherige temperatur?
        if (latestWeatherObj != null) {
            tempLastChecked = latestWeatherObj.getAir_temperature();
            timeLastChecked = latestWeatherObj.getTime_stamp_cet();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();

        if (service.isServiceBound()) {
            unbindService(serviceConnection);
        }
//        todo: service ends when Activity-onDestroy() called??
//        stopService(serviceIntent);
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
            startScheduledTask();
        }

        private void startScheduledTask() {
            tv_status_service.setText("Service On");
            service.setScheduledTask(new TimerTask() {
                @Override
                public void run() {
                    latestWeatherObj = WeatherAPI.fetchWeather();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (latestWeatherObj == null) {
                                Toast.makeText(getApplicationContext(),
                                        "check the Weather API website, it may not work", Toast.LENGTH_SHORT).show();
                            } else {
                                tv_note.setText(latestWeatherObj.toString());
                            }
                        }
                    });
                    Log.d(LOG_TAG, latestWeatherObj.toString());

//                    todo notification test
                    if (latestWeatherObj.getTime_stamp_cet().equals("27.09.2019 20:00:00")) {
                        WeatherNotification.runNotification(getApplicationContext());
                    }

                }
            });
        }

        // so far never called, onServiceDisconnected() is only called in extreme situations
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "onServiceDisconnected() called");
            service = null;
        }
    }
}
