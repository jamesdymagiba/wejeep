package com.example.wejeep;

import android.view.ViewGroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActiveModernJeepAdapter extends RecyclerView.Adapter<ActiveModernJeepAdapter.ViewHolder> {

    private List<ActiveModernJeepModel> activeModernJeepList;

    // Constructor
    public ActiveModernJeepAdapter(List<ActiveModernJeepModel> activeModernJeepList) {
        this.activeModernJeepList = activeModernJeepList;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView unitNumberTextView;
        public TextView vehicleModelTextView;
        public TextView driverNameTextView;
        public TextView paoNameTextView;
        public TextView plateNumberTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            unitNumberTextView = itemView.findViewById(R.id.tvUnitNumber);
            vehicleModelTextView = itemView.findViewById(R.id.tvVehicleModel);
            driverNameTextView = itemView.findViewById(R.id.tvDriverName);
            paoNameTextView = itemView.findViewById(R.id.tvPaoName);
            plateNumberTextView = itemView.findViewById(R.id.tvPlateNumber);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_modern_jeep_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveModernJeepModel jeep = activeModernJeepList.get(position);
        holder.unitNumberTextView.setText(jeep.getUnitNumber());
        holder.vehicleModelTextView.setText(jeep.getVehicleModel());
        holder.driverNameTextView.setText(jeep.getNameDriver());
        holder.paoNameTextView.setText(jeep.getNamePao());
        holder.plateNumberTextView.setText(jeep.getPlateNumber());
    }

    @Override
    public int getItemCount() {
        return activeModernJeepList.size();
    }
}
