package com.example.wejeep;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PAOAdapter extends RecyclerView.Adapter<PAOAdapter.PAOViewHolder> {

    private List<PAOModel> paoList;
    private FirebaseFirestore db;
    private FirebaseFunctions firebaseFunctions;

    // Constructor to accept Firestore instance
    public PAOAdapter(List<PAOModel> paoList, FirebaseFirestore db) {
        this.paoList = paoList;
        this.db = db;
        this.firebaseFunctions = FirebaseFunctions.getInstance();
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
        holder.tvDateAdded.setText(pao.getDateAdded()); // Set the date added
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
            // Retrieve the document from Firestore based on the document ID
            db.collection("users").document(paoId) // Use document ID to reference the specific PAO
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uid = paoId;  // Save paoId as uid
                            Log.d("PAOAdapter", "UID from Firestore: " + uid);

                            // Check if UID is not null or empty
                            if (uid != null && !uid.isEmpty()) {
                                // Pass UID inside a map to Firebase function
                                Map<String, Object> data = new HashMap<>();
                                data.put("uid", uid);  // Passing the UID as a key-value pair

                                // Call the Firebase function to delete the user account
                                Log.d("PAOAdapter", "UID from Firestore: " + uid);
                                Log.d("PAOAdapter", "paoid from Firestore: " + paoId);

                                firebaseFunctions
                               .getHttpsCallable("deleteUserAccount")
                                .call(data)  // Passing the map as the argument
                                        .addOnSuccessListener(result -> {
                                            // Remove the PAO from the list and update UI
                                            paoList.remove(position);
                                            notifyItemRemoved(position); // Notify the adapter that the item was removed
                                            Toast.makeText(context, "PAO deleted", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // On Failure
                                            Toast.makeText(context, "Error deleting user account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // If UID is null or empty
                                Toast.makeText(context, "UID is null or empty", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If the document doesn't exist
                            Toast.makeText(context, "PAO document does not exist in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If the document id is null
                        Toast.makeText(context, "Error retrieving PAO from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle case where PAO ID is null
            Toast.makeText(context, "PAO ID is null", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public int getItemCount() {
        return paoList.size();
    }

    static class PAOViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaoName, tvPaoEmail, tvDateAdded; // Added dateTextView
        Button btnDelete;

        PAOViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaoName = itemView.findViewById(R.id.tvPaoName);
            tvPaoEmail = itemView.findViewById(R.id.tvPaoEmail);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded); // Initialize dateTextView
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}