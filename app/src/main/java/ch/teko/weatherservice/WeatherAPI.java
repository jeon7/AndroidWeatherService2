package ch.teko.weatherservice;

import android.util.Log;

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

public class WeatherAPI {
    private static final String LOG_TAG = "Weather API";
    private static final String URL_API = "https://tecdottir.herokuapp.com/measurements/tiefenbrunnen";

    public static Weather fetchWeather() {
        Weather latestWeatherObj = null;
        try {
            URL url = new URL(URL_API);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            String responseJSON = buffer.toString();
            Log.d(LOG_TAG, responseJSON);

            latestWeatherObj = getParsedJSONElements(responseJSON);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latestWeatherObj;
    }

    private static Weather getParsedJSONElements(String responseJSON) {
        Weather latestWeatherObj = null;
        try {
            JSONObject jsonObject = new JSONObject(responseJSON);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            int lastElement = jsonArray.length() - 1;
            JSONObject jsonObject_newest_data = jsonArray.getJSONObject(lastElement);

            String station = jsonObject_newest_data.getString("station");
            Log.d(LOG_TAG, station);

            JSONObject jsonObject_values = jsonObject_newest_data.getJSONObject("values");
            String time_stamp_cet = jsonObject_values.getJSONObject("timestamp_cet").getString("value");
            Log.d(LOG_TAG, time_stamp_cet);
            String air_temperature = jsonObject_values.getJSONObject("air_temperature").getString("value");
            Log.d(LOG_TAG, air_temperature);

            latestWeatherObj = new Weather(station, time_stamp_cet, air_temperature);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return latestWeatherObj;
    }
}
