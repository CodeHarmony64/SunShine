package com.example.njgupta.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment{
    final String URL_FORMED = "URL Formed";
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateListView();
    }

    public void populateListView(){
        ListView listview = (ListView)getView().findViewById(R.id.listView_forecast);
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
};

