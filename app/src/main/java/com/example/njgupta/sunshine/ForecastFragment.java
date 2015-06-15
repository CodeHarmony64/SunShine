package com.example.njgupta.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment{
    final String URL_FORMED = "URL Formed";
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        populateListView(rootView);
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
          //do a refresh
            Log.v(ForecastFragment.class.getName(),"Refreshing the listview");
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute("daily","London","7","json");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void populateListView(View rootView){
        Log.v(ForecastFragment.class.getName(),"Populating the List View");
        ListView listview = (ListView)rootView.findViewById(R.id.listView_forecast);
        List<String> forecastArray = new ArrayList<>();
        forecastArray.add("Rainy");
        forecastArray.add("Bright");
        forecastArray.add("ThunderStorm");
        forecastArray.add("Sunny");
        forecastArray.add("Cloudy");
        forecastArray.add("Hot");
        forecastArray.add("Cold");
        ArrayAdapter<String> forecastArrayAdapter = new ArrayAdapter<String>(this.getActivity(),R.layout.forecast_list_item_layout,
                R.id.textView_forecast_listView_item,forecastArray);
        listview.setAdapter(forecastArrayAdapter);
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,Void>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/";
        private final String PARAM_FORECAST_URI_AUTHORITY = "api.openweathermap.org";
        private final String PARAM_FORECAST_URI_SCHEME ="http";
        private final String [] PARAM_FORECAST_APPEND_PATH= {"data","2.5","forecast"};
        private final String PARAM_FORECAST_CITY = "q";
        private final String PARAM_FORECAST_MODE = "mode";
        private final String PARAM_FORECAST_UNITS = "units";
        private final String PARAM_FORECAST_COUNT = "cnt";


        @Override
        protected Void doInBackground(String... params) {
            //do nothing for now
            Log.v(ForecastFragment.class.getName(),"Async Task: Do In Background");
            fetchWeather(params[0],params[1],params[2],params[3]);
            return null;
        }

        public String fetchWeather(String... param){ //param[0] : daily or null
                                                    //param[1] : city
                                                    // param[2] : count
                                                    // param[3] : forecast mode json or xml
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            Log.v(ForecastFragment.class.getName(),"Async Task -> DoInBackground -> fetchWeather");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
               // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                Uri.Builder forecastUriBuilder =  new Uri.Builder();

                forecastUriBuilder
                        .scheme(PARAM_FORECAST_URI_SCHEME)
                        .authority(PARAM_FORECAST_URI_AUTHORITY)
                        .appendPath(PARAM_FORECAST_APPEND_PATH[0])
                        .appendPath(PARAM_FORECAST_APPEND_PATH[1])
                        .appendPath(PARAM_FORECAST_APPEND_PATH[2]);
                if(param[0] != null)
                    forecastUriBuilder.appendPath(param[0]);
                forecastUriBuilder
                        .appendQueryParameter(PARAM_FORECAST_CITY,param[1])
                        .appendQueryParameter(PARAM_FORECAST_COUNT,param[2])
                        .appendQueryParameter(PARAM_FORECAST_MODE,param[3])
                        .appendQueryParameter(PARAM_FORECAST_UNITS,"metric");
                URL url = new URL(forecastUriBuilder.build().toString());
                Log.v(ForecastFragment.class.getName(),"Forecast query: "+url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Forecast Json String:  "+forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return forecastJsonStr;
        }
    }
};

