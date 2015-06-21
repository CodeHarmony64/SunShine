package com.example.njgupta.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment{
    final String URL_FORMED = "URL Formed";
    public static final String EXTRA_DAY_FORECAST ="com.example.sunshine.EXTRA_DAY_FORECAST";
    ArrayAdapter mForecastAdapter;
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
        View rootView =  inflater.inflate(R.layout.forecast_fragment, container, false);
        populateListView(rootView,getDummyListViewData());
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

    public static List<String> getDummyListViewData(){
        List<String> forecastArray = new ArrayList<>();
        forecastArray.add("Rainy");
        forecastArray.add("Bright");
        forecastArray.add("ThunderStorm");
        forecastArray.add("Sunny");
        forecastArray.add("Cloudy");
        forecastArray.add("Hot");
        forecastArray.add("Cold");
        return forecastArray;
    }

    public void populateListView(View rootView,List<String> forecastArray){
        ListView   listView = (ListView)rootView.findViewById(R.id.listView_forecast);
        mForecastAdapter= new ArrayAdapter<String>(this.getActivity(),R.layout.forecast_list_item_layout,
                R.id.textView_forecast_listView_item,forecastArray);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getActivity().getApplicationContext();
                TextView textView = (TextView)view;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context,textView.getText(),duration);
                toast.show();
                Intent intent = new Intent(getActivity(),DetailsActivity.class);
                intent.putExtra(EXTRA_DAY_FORECAST,parent.getItemAtPosition(position).toString());
                startActivity(intent);

            }
        });
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

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
        protected String[] doInBackground(String... params) {
            //do nothing for now
            Log.v(ForecastFragment.class.getName(), "Async Task: Do In Background");
            String forecastJsonStr = fetchWeather(params[0],params[1],params[2],params[3]);
            String[] forecast;
            try {
                forecast = getWeatherDataFromJson(forecastJsonStr, Integer.parseInt(params[2]));
            }catch (JSONException je){
                Log.e(this.LOG_TAG,"Not able to get weather data from JSON String");
                return null;
            }
            return forecast;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result !=null){
                mForecastAdapter.clear();
                for(String dayForecastStr : result){
                    mForecastAdapter.add(dayForecastStr);
                }
            }

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

        /* The date/time conversion code is going to be moved outside the asynctask later,
       * so for convenience we're breaking it out into its own method now.
       */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);


            return roundedHigh + "/" + roundedLow;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day+"," + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }
    }
}

