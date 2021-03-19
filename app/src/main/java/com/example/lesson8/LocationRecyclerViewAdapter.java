package com.example.lesson8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final List<LocationModel> locationsList;

    public LocationRecyclerViewAdapter(Context context, List<LocationModel> locationsList) {
        this.context = context;
        this.locationsList = locationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.i("dev", "onCreateViewHolder()");
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.location_list_item, parent, false);
        return new LocationRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //  Log.i("dev", "onBindViewHolder() " + locationsList.get(position).getLatitude());
        holder.textViewLatitude.setText(String.valueOf(locationsList.get(position).getLatitude()));
        holder.textViewLongitude.setText(String.valueOf(locationsList.get(position).getLongitude()));
    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewLongitude;
        private final TextView textViewLatitude;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLatitude = itemView.findViewById(R.id.textView_listItem_latitude);
            textViewLongitude = itemView.findViewById(R.id.textView_listItem_longitude);

        }
    }

}
