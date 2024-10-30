package com.example.wejeep;

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

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {
    private ArrayList<DriverModel> driverList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DriverAdapter(ArrayList<DriverModel> driverList) { this.driverList = driverList;}

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_card_view, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        DriverModel driver = driverList.get(position);
        holder.tvDriverName.setText(driver.getName());
        holder.tvDriverContact.setText(driver.getContact());
        holder.tvDateAdded.setText(driver.getDateAdded());

        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminEditDriver.class);
            intent.putExtra("documentId", driver.getDocumentId()); // Pass the document ID
            intent.putExtra("driverName", driver.getName()); // Pass driver's name
            intent.putExtra("driverContact", driver.getContact()); // Pass driver's contact
            intent.putExtra("dateAdded", driver.getDateAdded()); // Pass date added
            ((AdminManageDriver) holder.itemView.getContext()).startActivityForResult(intent, 100); // Start for result with a request code
        });

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(driver.getDocumentId(), position, holder.itemView.getContext()); // Show confirmation dialog
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvDriverContact, tvDateAdded;
        Button btnEdit, btnDelete;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvDriverContact = itemView.findViewById(R.id.tvDriverContact);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method to show a confirmation dialog before deleting
    private void showDeleteConfirmationDialog(String driverId, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Driver")
                .setMessage("Are you sure you want to delete this driver?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteDriver(driverId, position, context); // Proceed with delete
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .show();
    }

    // Method to delete driver from Firestore and update RecyclerView
    private void deleteDriver(String driverId, int position, Context context) {
        if (driverId != null) {
            db.collection("drivers").document(driverId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the unit from the list
                        driverList.remove(position);
                        // Notify the adapter about the removed item
                        notifyItemRemoved(position);
                        // Notify about item change in case of position changes
                        notifyItemRangeChanged(position, driverList.size());
                        Toast.makeText(context, "Driver deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting driver", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Driver ID is null", Toast.LENGTH_SHORT).show();
        }
    }
}
