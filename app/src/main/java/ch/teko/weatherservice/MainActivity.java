package ch.teko.weatherservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static TextView tv_status_internet;
    private static TextView tv_status_service;
    private static TextView tv_note;
    private static EditText et_temp_diff;
    private static Button btnStartService;
    private static Button btnEndService;

    private Intent weatherServiceIntent;
    private Intent connectivityServiceIntent;

    private String tempDiff;
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

    public void onButtonStartServiceClicked(View view) {
        Log.d(LOG_TAG, "onButtonStartServiceClicked() called");
        et_temp_diff.clearFocus();
        if (ConnectivityReceiver.isConnected()) {
            if (et_temp_diff.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(),
                        "type temperature difference first for notification", Toast.LENGTH_SHORT).show();
            } else {
                weatherServiceIntent = new Intent(this, WeatherService.class);
                startService(weatherServiceIntent);
                updateUserInputTempDiff();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "check internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onButtonEndServiceClicked(View view) {
        Log.d(LOG_TAG, "onButtonEndServiceClicked() called");
        Log.d(LOG_TAG, "isServiceStarted()=" + WeatherService.isServiceStarted());

        if (WeatherService.isServiceStarted()) {
            stopService(weatherServiceIntent);
        }
        Log.d(LOG_TAG, "isServiceStarted()=" + WeatherService.isServiceStarted());
    }

    private void updateUserInputTempDiff() {
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

    protected static void setUI() {
        Log.d(LOG_TAG, "setUI() called");
        // internet status
        if (ConnectivityReceiver.isConnected()) {
            tv_status_internet.setText("connected to Internet");
        } else {
            tv_status_internet.setText("no Internet");
        }
        // service status, buttons
        if (WeatherService.isServiceStarted()) {
            tv_status_service.setText("Service is on");
            btnStartService.setEnabled(false);
            btnEndService.setEnabled(true);
        } else {
            tv_status_service.setText("Service is off");
            btnStartService.setEnabled(true);
            btnEndService.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
