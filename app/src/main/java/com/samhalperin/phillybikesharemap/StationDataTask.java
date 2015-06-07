package com.samhalperin.phillybikesharemap;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sqh on 5/8/15.
 */
public class StationDataTask extends AsyncTask<String, Void, Station[]> {

    MapsActivity mHandler;

    public StationDataTask(MapsActivity context) {
        mHandler = context;
    }

    private static boolean DEBUG = false;

    @Override
    protected Station[] doInBackground(String... params) {

        if (DEBUG) {
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return debuggingStations();
        } else {
            try {
                String jsonResponse = doGetRequest(params[0]);
                Station[] stations = StationJsonResponseParser.jsonToStations(jsonResponse);
                StationPersistence.getInstance(mHandler).saveStations(jsonResponse);
                return stations;
            } catch (IOException e) {
                Log.w("pbsm", "Problem fetching station data", e);
            }
            Log.e("pbsm", "Uh oh, fell through to nodata.");
            return new Station[0];
        }
    }

    private String doGetRequest(String urlStr) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuffer responseData = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            return responseData.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Station[] debuggingStations() {
        return new Station[]  {

                new Station(MapsActivity.PHILLY,
                        "1234 fake street",
                        20,
                        30,
                        "Available"
                ),
                new Station(MapsActivity.PHILLY,
                        "1234 fake street",
                        20,
                        30,
                        "Debug"
                ),
                new Station(
                        new LatLng(39.95378, -75.16374),
                        "1401 JFK debug",
                        30,
                        40,
                        "ComingSoon"
                ),
                new Station(
                        new LatLng(39.93378, -75.16374),
                        "5586 unreal street",
                        30,
                        40,
                        "PartialService"
                ),
                new Station(
                        new LatLng(39.91378, -75.16374),
                        "987 philly street",
                        30,
                        40,
                        "Unavailable"
                ),
                new Station(
                        new LatLng(39.88378, -75.16374),
                        "987 philly street",
                        30,
                        40,
                        "SpecialEvent"
                )


        };
    }

    @Override
    protected void onPreExecute() {
        setStatusText("Loading station data...");
    }

    @Override
    protected void onPostExecute(Station[] stations) {
        String lastUpdated = getLastUpdatedText();
        if (stations.length == 0) {
            Station[] previousResults = StationPersistence.getInstance(mHandler).getPreviousStations();
            if (previousResults != null) {
                Toast.makeText(mHandler, "Refresh failed; showing previous stations", Toast.LENGTH_LONG).show();
                setStatusText("Showing previous station data (" + lastUpdated + ")");
                stations = previousResults;
            } else {
                setStatusText("Unable to load station data");
            }
        } else {
            setStatusText("Station data loaded at " + lastUpdated);
        }

        mHandler.loadStationData(stations);
    }

    private String getLastUpdatedText() {
        Date lastUpdate = StationPersistence.getInstance(mHandler).getLastUpdateTime();
        return new SimpleDateFormat("h:mm aa").format(lastUpdate);
    }

    private void setStatusText(String msg) {
        TextView statusTextView = (TextView) mHandler.findViewById(R.id.statusTextView);
        statusTextView.setText(msg);
    }

    public interface StationDataLoader{
        void loadStationData(Station[] stations);
    }

}
