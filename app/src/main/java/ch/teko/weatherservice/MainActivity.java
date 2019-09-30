package ch.teko.weatherservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static TextView tv_status_internet;
    private TextView tv_status_service;
    private TextView tv_note;
    private EditText et_temp_diff;
    private Button btnStartService;
    private Button btnEndService;

    private WeatherService weatherService;
    private Intent weatherServiceIntent;
    private ServiceConnection weatherServiceConnection;
    private Intent connectivityServiceIntent;

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

        // to remember user input (temperature difference)
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String spLastValue = sharedPreferences.getString(SHARED_PREFERENCE_TEMP_DIFF, "");

        // show last user saved temperature difference
        et_temp_diff = findViewById(R.id.editText_temperature_diff);
        et_temp_diff.setText(spLastValue);

        tv_status_internet = findViewById(R.id.tv_status_internet);
        tv_status_service = findViewById(R.id.tv_status_service);
        tv_note = findViewById(R.id.tv_note);

        btnStartService = findViewById(R.id.button_start_service);
        btnEndService = findViewById(R.id.button_end_service);

        // ConnectivityService will register connectivityBroadcastReceiver
        // this should be always running even before weather service started
        // in order to display internet connectivity
        connectivityServiceIntent = new Intent(getApplicationContext(), ConnectivityService.class);
        startService(connectivityServiceIntent);


        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_temp_diff.clearFocus();
                btnStartService.setEnabled(false);
                btnEndService.setEnabled(true);
                if (ConnectivityReceiver.isConnected()) {
                    if (et_temp_diff.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "type temperature difference first for notification", Toast.LENGTH_SHORT).show();
                    } else {
                        weatherServiceIntent = new Intent(getApplicationContext(), WeatherService.class);
                        startService(weatherServiceIntent);
                        weatherServiceConnection = new WeatherServiceConnection();
                        bindService(weatherServiceIntent, weatherServiceConnection, Context.BIND_AUTO_CREATE);
                        updateTempDiff();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "check internet connection", Toast.LENGTH_SHORT).show();
                }
                setUI();
            }
        });

        btnEndService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEndService.setEnabled(false);
                btnStartService.setEnabled(true);

                if (weatherService != null) {
                    unbindService(weatherServiceConnection);
                    weatherService = null;
                }

                if (WeatherService.isServiceStarted()) {
                    stopService(weatherServiceIntent);
                }
                setUI();
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
        setUI();

    }

    private void setUI() {
        // internet status
        if (ConnectivityReceiver.isConnected()) {
            tv_status_internet.setText("connected to Internet");
        } else {
            tv_status_internet.setText("no Internet");
        }
        // service status
        if (WeatherService.isServiceStarted()) {
            tv_status_service.setText("Service is on");
        } else {
            tv_status_service.setText("Service is off");
        }

        // buttons
        if (WeatherService.isServiceStarted()) {
            btnStartService.setEnabled(false);
            btnEndService.setEnabled(true);
        } else {
            btnStartService.setEnabled(true);
            btnEndService.setEnabled(false);
        }
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

        if (weatherService != null) {
            unbindService(weatherServiceConnection);
            weatherService = null;
        }
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
            weatherService = ((WeatherService.LocalBinder) iBinder).getService();
        }

        // so far never called, onServiceDisconnected() is only called in extreme situations
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "onServiceDisconnected() called");
            weatherService = null;
        }
    }
}
