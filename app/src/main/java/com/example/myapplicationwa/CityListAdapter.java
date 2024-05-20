package com.example.myapplicationwa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> {

    private List<CityEntity> cities;
    private String searchedCity;

    public CityListAdapter(List<CityEntity> cities, String searchedCity) {
        this.cities = cities;
        this.searchedCity = searchedCity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View cityView = inflater.inflate(R.layout.item_city, parent, false);
        return new ViewHolder(cityView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityEntity cityEntity = cities.get(position);
        holder.cityNameTextView.setText(cityEntity.getCity());
        holder.tempTextView.setText(cityEntity.getTemp());
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityNameTextView;
        TextView tempTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cityNameTextView = itemView.findViewById(R.id.textViewC);
            tempTextView = itemView.findViewById(R.id.temp);
        }
    }

    public void moveToFirstPosition() {
        if (searchedCity != null && !searchedCity.isEmpty()) {
            for (int i = cities.size() - 1; i >= 0; i--) {
                CityEntity cityEntity = cities.get(i);
                Log.d("CityListAdapter", "Comparing: " + cityEntity.getCity() + " with " + searchedCity);
                if (cityEntity.getCity().equalsIgnoreCase(searchedCity)) {
                    Log.d("CityListAdapter", "Match found. Moving " + cityEntity.getCity() + " to first position");
                    cities.remove(i);
                    cities.add(0, cityEntity);
                    notifyDataSetChanged();  // Notify the adapter about the data change
                    break;
                }
            }
        }
    }
}
