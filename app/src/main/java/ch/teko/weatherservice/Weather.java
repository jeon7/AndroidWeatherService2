package ch.teko.weatherservice;

import androidx.annotation.NonNull;

public class Weather {
    private String station;
    private String time_stamp_cet;
    private String air_temperature;

    public Weather(String station, String time_stamp_cet, String air_temperature) {
        this.station = station;
        this.time_stamp_cet = time_stamp_cet;
        this.air_temperature = air_temperature;
    }

    public String getTime_stamp_cet() {
        return time_stamp_cet;
    }

    public String getAir_temperature() {
        return air_temperature;
    }

    @NonNull
    @Override
    public String toString() {
        String weatherSting = "station: " + station +
                "\ntime_stamp_cet: " + time_stamp_cet +
                "\nair_temperature: " + air_temperature;
        return weatherSting;
    }
}