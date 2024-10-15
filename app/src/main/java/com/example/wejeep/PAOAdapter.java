package com.example.wejeep;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PAOAdapter extends RecyclerView.Adapter<PAOAdapter.PAOViewHolder> {

    private List<PAOModel> paoList;
    private FirebaseFirestore db; // Initialize Firestore instance

    // Constructor to accept Firestore instance
    public PAOAdapter(List<PAOModel> paoList, FirebaseFirestore db) {
        this.paoList = paoList;
        this.db = db; // Set Firestore instance
    }

    @NonNull
    @Override
    public PAOViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pao_card_view, parent, false);
        return new PAOViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PAOViewHolder holder, int position) {
        PAOModel pao = paoList.get(position);
        holder.tvPaoName.setText(pao.getName());
        holder.tvPaoEmail.setText(pao.getEmail());

        // Set up the delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(pao.getDocumentId(), position, holder.itemView.getContext());
        });
    }

    // Method to show a confirmation dialog before deleting
    private void showDeleteConfirmationDialog(String paoId, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PAO")
                .setMessage("Are you sure you want to delete this PAO?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deletePAO(paoId, position, context); // Proceed with delete after confirmation
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .show();
    }

    // Method to delete PAO from Firestore and update RecyclerView
    private void deletePAO(String paoId, int position, Context context) {
        if (paoId != null) {
            // Remove the item from the Firestore database
            db.collection("users").document(paoId) // Use document ID to reference the specific PAO
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the item from the list
                        paoList.remove(position);
                        notifyItemRemoved(position); // Notify the adapter of the removed item
                        Toast.makeText(context, "PAO deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting PAO: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "PAO ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return paoList.size();
    }

    static class PAOViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaoName, tvPaoEmail;
        Button btnDelete;

        PAOViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaoName = itemView.findViewById(R.id.tvPaoName);
            tvPaoEmail = itemView.findViewById(R.id.tvPaoEmail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
