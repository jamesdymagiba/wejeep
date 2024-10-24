package com.example.wejeep;

import android.content.Context;
import android.content.Intent;
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

public class AssignAdapter extends RecyclerView.Adapter<AssignAdapter.AssignViewHolder> {
    private ArrayList<AssignModel> assignList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AssignAdapter(ArrayList<AssignModel> assignList) {
        this.assignList = assignList;
    }

    @NonNull
    @Override
    public AssignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assign_card_view, parent, false);
        return new AssignViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AssignViewHolder holder, int position) {
        AssignModel assign = assignList.get(position);

        holder.tvFromDay.setText(assign.getFromDay());
        holder.tvToDay.setText(assign.getToDay());
        holder.tvFromTime.setText(assign.getFromTime());
        holder.tvToTime.setText(assign.getToTime());
        holder.tvUnitNumber.setText(assign.getUnitNumber());
        holder.tvDriverName.setText(assign.getDriver());
        holder.tvPlateNumber.setText(assign.getPlateNumber());
        // If the conductor field is in use in the future, uncomment the line below
        // holder.tvConductorName.setText(assign.getConductor())
    // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminEditAssignedUnitScreen.class);
            intent.putExtra("documentId", assign.getDocumentId());
            intent.putExtra("fromDay", assign.getFromDay());
            intent.putExtra("toDay", assign.getToDay());
            intent.putExtra("fromTime", assign.getFromTime());
            intent.putExtra("toTime", assign.getToTime());
            intent.putExtra("unitNumber", assign.getUnitNumber());
            intent.putExtra("driverName", assign.getDriver());
            intent.putExtra("plateNumber", assign.getPlateNumber());
            ((AdminManageActiveUnitList) holder.itemView.getContext()).startActivityForResult(intent, 100);
        });

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> deleteAssign(position, holder.itemView.getContext()));
    }

    @Override
    public int getItemCount() {
        return assignList.size();
    }

    public class AssignViewHolder extends RecyclerView.ViewHolder {
        TextView tvFromDay, tvToDay, tvFromTime, tvToTime, tvUnitNumber, tvDriverName, tvConductorName, tvPlateNumber;
        Button btnEdit, btnDelete;

        public AssignViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize TextViews
            tvFromDay = itemView.findViewById(R.id.tvFromDay);
            tvToDay = itemView.findViewById(R.id.tvToDay);
            tvFromTime = itemView.findViewById(R.id.tvFromTime);
            tvToTime = itemView.findViewById(R.id.tvToTime);
            tvUnitNumber = itemView.findViewById(R.id.tvUnitNumber);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvConductorName = itemView.findViewById(R.id.tvConductorName);
            tvPlateNumber = itemView.findViewById(R.id.tvPlateNumber);

            // Initialize Buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method to remove a card from the RecyclerView and Firestore
    private void deleteAssign(int position, Context context) {
        AssignModel assign = assignList.get(position);

        // Remove the item from Firestore
        db.collection("assign") // Replace with your actual Firestore collection name
                .document(assign.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the item from the list
                    assignList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, assignList.size());
                    Toast.makeText(context, "Card removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error removing card", Toast.LENGTH_SHORT).show();
                });
    }
}
