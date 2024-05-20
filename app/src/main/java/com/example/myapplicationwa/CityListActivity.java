package com.example.myapplicationwa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CityListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load cities from the database
        loadCityList();
    }

    private void loadCityList() {
        new LoadCityTask().execute();
    }

    private class LoadCityTask extends AsyncTask<Void, Void, List<CityEntity>> {
        @Override
        protected List<CityEntity> doInBackground(Void... voids) {
            DatabaseHelper database = DatabaseHelper.getDB(CityListActivity.this);

            return database.cityDao().getAllCity();
        }

        @Override
        protected void onPostExecute(List<CityEntity> cityEntities) {
            super.onPostExecute(cityEntities);
            String lastSearchedCity = getLastSearchedCity();
            for (int i = (cityEntities.size())-1; i>=0; i --){

            }
            adapter = new CityListAdapter(cityEntities, lastSearchedCity);
            recyclerView.setAdapter(adapter);
            adapter.moveToFirstPosition();
        }
    }

    private String getLastSearchedCity() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        String userCity = sharedPreferences.getString("userCity", "Mumbai");
        Log.d("CityListActivity", "Last searched city: " + userCity);
        return userCity;
    }
}
