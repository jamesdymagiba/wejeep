package com.example.wejeep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private List<DriverModel> driverList;

    public DriverAdapter(List<DriverModel> driverList) {
        this.driverList = driverList;
    }

    @Override
    public DriverViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_card_view, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DriverViewHolder holder, int position) {
        DriverModel driver = driverList.get(position);
        holder.tvDriverName.setText(driver.getName());
        holder.tvDriverContact.setText(driver.getContact());
        holder.tvDateAdded.setText(driver.getDateAdded());
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvDriverContact, tvDateAdded;

        public DriverViewHolder(View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvDriverContact = itemView.findViewById(R.id.tvDriverContact);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
        }
    }
}

