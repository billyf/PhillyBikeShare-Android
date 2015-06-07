package com.samhalperin.phillybikesharemap;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class StationPersistence {

    private static final String PREFS_NAME = "KiosksPrefs";
    private final SharedPreferences sharedPreferences;

    public static StationPersistence getInstance(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        return new StationPersistence(sharedPreferences);
    }

    private StationPersistence(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public Station[] getPreviousStations() {
        String lastStationResponse = sharedPreferences.getString("lastStationResponse", null);
        if (lastStationResponse != null) {
            Station[] stations = StationJsonResponseParser.jsonToStations(lastStationResponse);
            return stations;
        } else {
            return null;
        }
    }

    public Date getLastUpdateTime() {
        long resultTimestamp = sharedPreferences.getLong("resultTimestamp", 0);
        return new Date(resultTimestamp);
    }

    public void saveStations(String jsonResponse) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastStationResponse", jsonResponse);
        editor.putLong("resultTimestamp", System.currentTimeMillis());
        editor.commit();
    }
}
