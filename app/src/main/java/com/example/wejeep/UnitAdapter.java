package com.example.wejeep;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {
    private ArrayList<UnitModel> unitList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UnitAdapter(ArrayList<UnitModel> unitList) {
        this.unitList = unitList;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_card_view, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        UnitModel unit = unitList.get(position);
        holder.tvVehicleModel.setText(unit.getvehicleModel());
        holder.tvPlateNumber.setText(unit.getplateNumber());
        holder.tvUnitNumber.setText(unit.getunitNumber());
        holder.tvDateAdded.setText(unit.getDateAdded());

        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminEditUnitScreen.class);
            intent.putExtra("documentId", unit.getDocumentId()); // Pass the document ID
            intent.putExtra("vehicleUnit", unit.getvehicleModel()); // Pass vehicle unit
            intent.putExtra("plateNumber", unit.getplateNumber()); // Pass platenumber
            intent.putExtra("unitNumber", unit.getunitNumber());
            intent.putExtra("dateAdded", unit.getDateAdded()); // Pass date added
            ((AdminManageUnitScreen) holder.itemView.getContext()).startActivityForResult(intent, 100); // Start for result with a request code
        });


        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            deleteUnit(unit.getDocumentId(), position, holder.itemView.getContext()); // Pass context
        });
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public class UnitViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleModel, tvPlateNumber,tvUnitNumber, tvDateAdded;
        Button btnEdit, btnDelete;

        public UnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleModel = itemView.findViewById(R.id.tvVehicleModel);
            tvPlateNumber = itemView.findViewById(R.id.tvPlateNumber);
            tvUnitNumber = itemView.findViewById(R.id.tvUnitNumber);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method to delete driver from Firestore and update RecyclerView
    private void deleteUnit(String unitId, int position, Context context) {
        if (unitId != null) {
            db.collection("units").document(unitId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the unit from the list
                        unitList.remove(position);
                        // Notify the adapter about the removed item
                        notifyItemRemoved(position);
                        // Notify about item change in case of position changes
                        notifyItemRangeChanged(position, unitList.size());
                        Toast.makeText(context, "Unit deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting unit", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Unit ID is null", Toast.LENGTH_SHORT).show();
        }
    }
}

