package com.example.myapplicationwa;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String API = "06c921750b9a82d8f5d1294e1586276f"; // Use your API key

    Switch switcher;
    ImageView menuIcon;
    boolean nightMODE;
    private Switch notificationSwitch;
    private NotificationManager notificationManager;
    private List<String> cityList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switcher = findViewById(R.id.switcher);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMODE = sharedPreferences.getBoolean("night", false);

        if (nightMODE) {
            switcher.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMODE) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", true);
                }
                editor.apply();
            }
        });

        ImageView leftIcon = findViewById(R.id.left_icon);
        ImageView rightIcon = findViewById(R.id.right_icon);
        TextView title = findViewById(R.id.toolbar_title);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityListActivity(cityList);
            }
        });

        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        notificationSwitch = findViewById(R.id.notificationSwitch);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Save the city name to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userCity", query);
                editor.apply();

                // Fetch weather data immediately
                new WeatherTask().execute(query);

                saveList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancelAll();

        // Load cities from the database
        loadCityList();

        menuIcon = findViewById(R.id.menuIcon);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch the city name and temperature from the UI elements or stored variables
                TextView cityTextView = findViewById(R.id.address);
                TextView tempTextView = findViewById(R.id.temp);

                String cityName = cityTextView.getText().toString();
                String temperature = tempTextView.getText().toString();

                // Create the intent and put the city name and temperature as extras
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "City: " + cityName + ", Temperature: " + temperature);

                // Start the activity to share the data
                startActivity(Intent.createChooser(sendIntent, "Share to: "));
            }
        });

        // Check if the activity was started with the intent to refresh settings
        if (getIntent().getBooleanExtra("refresh", false)) {
            refreshSettings(null); // Call refreshSettings with a delay
        }
    }

    public void refreshSettings(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String userCity = sharedPreferences.getString("userCity", "Paris");

        // Use a Handler to introduce a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchWeatherData(userCity);
            }
        }, 5000); // 5000 milliseconds delay (5 seconds)
    }

    // Method to fetch weather data
    private void fetchWeatherData(String city) {
        new WeatherTask().execute(city);
    }

    private void saveList(String city) {
        new SaveCityTask().execute(city);
    }

    private void loadCityList() {
        new LoadCityTask().execute();
    }

    private class SaveCityTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... cities) {
            String city = cities[0];
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            CityEntity newCityEntity = new CityEntity(city, "default_temp");
            database.cityDao().addCity(newCityEntity);
            return null;
        }
    }

    private class LoadCityTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            List<CityEntity> cityEntities = database.cityDao().getAllCity();
            List<String> cities = new ArrayList<>();
            for (CityEntity cityEntity : cityEntities) {
                cities.add(cityEntity.getCity());
            }
            return cities;
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            super.onPostExecute(cities);
            cityList.clear();
            cityList.addAll(cities);
        }
    }

    private void showCityListActivity(List<String> cities) {
        Intent intent = new Intent(MainActivity.this, CityListActivity.class);
        intent.putStringArrayListExtra("cities", new ArrayList<>(cities));
        startActivity(intent);
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... cities) {
            String response;
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + cities[0] + "&units=metric&appid=" + API);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                response = stringBuilder.toString();
                connection.disconnect();
            } catch (IOException e) {
                response = null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject sys = jsonObject.getJSONObject("sys");
                JSONObject wind = jsonObject.getJSONObject("wind");
                JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObject.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");

                String address = jsonObject.getString("name") + ", " + sys.getString("country");
                String notificationContent = "Today's weather in " + address + " is " + temp + ".";

                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "weather_channel_id")
                        .setSmallIcon(R.drawable.weather_icon)
                        .setContentTitle("Weather Update")
                        .setContentText(notificationContent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Set the content intent
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                // Notify the notification manager
                int notificationId = 0;
                notificationManager.notify(notificationId, builder.build());

                ((TextView) findViewById(R.id.address)).setText(address);
                ((TextView) findViewById(R.id.updated_at)).setText(updatedAtText);
                ((TextView) findViewById(R.id.status)).setText(weatherDescription.toUpperCase());
                ((TextView) findViewById(R.id.temp)).setText(temp);
                ((TextView) findViewById(R.id.temp_min)).setText(tempMin);
                ((TextView) findViewById(R.id.temp_max)).setText(tempMax);
                ((TextView) findViewById(R.id.sunrise)).setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                ((TextView) findViewById(R.id.sunset)).setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                ((TextView) findViewById(R.id.wind)).setText(windSpeed);
                ((TextView) findViewById(R.id.pressure)).setText(pressure);
                ((TextView) findViewById(R.id.humidity)).setText(humidity);

                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

                // Save weather data to the database
                saveWeatherDataToDatabase(address, temp);

            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }

        private void saveWeatherDataToDatabase(String city, String temp) {
            new SaveWeatherDataTask().execute(new CityEntity(city, temp));
        }
    }

    private class SaveWeatherDataTask extends AsyncTask<CityEntity, Void, Void> {
        @Override
        protected Void doInBackground(CityEntity... cityEntities) {
            CityEntity cityEntity = cityEntities[0];
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            database.cityDao().addCity(cityEntity);
            return null;
        }
    }
}
