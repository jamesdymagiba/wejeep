package com.dygroup.wejeep;

import android.app.AlertDialog;
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

        holder.tvFromDay.setText(assign.getFromday());
        holder.tvToDay.setText(assign.getToday());
        holder.tvFromTime.setText(assign.getFromtime());
        holder.tvToTime.setText(assign.getTotime());
        holder.tvunitnumber.setText(assign.getunitnumber());
        holder.tvDriverName.setText(assign.getDriver());
        holder.tvplatenumber.setText(assign.getplatenumber());
        holder.tvConductorName.setText(assign.getConductor());
        holder.tvSchedule.setText(assign.getSchedule());
        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminEditAssignedUnitScreen.class);
            intent.putExtra("documentId", assign.getDocumentId());
            intent.putExtra("Fromday", assign.getFromday());
            intent.putExtra("Today", assign.getToday());
            intent.putExtra("Fromtime", assign.getFromtime());
            intent.putExtra("Totime", assign.getTotime());
            intent.putExtra("unitnumber", assign.getunitnumber());
            intent.putExtra("driverName", assign.getDriver());
            intent.putExtra("platenumber", assign.getplatenumber());
            intent.putExtra("schedule", assign.getSchedule());
            ((AdminManageActiveUnitList) holder.itemView.getContext()).startActivityForResult(intent, 100);
        });

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(assign.getDocumentId(), position, holder.itemView.getContext()); // Show confirmation dialog
        });
    }

    @Override
    public int getItemCount() {
        return assignList.size();
    }

    public class AssignViewHolder extends RecyclerView.ViewHolder {
        TextView tvFromDay, tvToDay, tvFromTime, tvToTime, tvunitnumber, tvDriverName, tvConductorName, tvplatenumber, tvSchedule;
        Button btnEdit, btnDelete;

        public AssignViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize TextViews
            tvFromDay = itemView.findViewById(R.id.tvFromDay);
            tvToDay = itemView.findViewById(R.id.tvToDay);
            tvFromTime = itemView.findViewById(R.id.tvFromTime);
            tvToTime = itemView.findViewById(R.id.tvToTime);
            tvunitnumber = itemView.findViewById(R.id.tvunitnumber);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvConductorName = itemView.findViewById(R.id.tvConductorName);
            tvplatenumber = itemView.findViewById(R.id.tvplatenumber);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);

            // Initialize Buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showDeleteConfirmationDialog(String assignId, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Assigned")
                .setMessage("Are you sure you want to delete this assigned?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteAssign(assignId, position, context); // Proceed with delete
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .show();
    }

    // Method to remove a card from the RecyclerView and Firestore
    private void deleteAssign(String assignId, int position, Context context) {
        AssignModel assign = assignList.get(position);
        if (assignId != null) {
            // Remove the item from Firestore
            db.collection("assigns").document(assign.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the item from the list
                        assignList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, assignList.size());
                        Toast.makeText(context, "Assigned removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error removing Assigned", Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
