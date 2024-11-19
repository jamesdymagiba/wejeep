package com.example.wejeep;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PAOAdapter extends RecyclerView.Adapter<PAOAdapter.PAOViewHolder> {

    private List<PAOModel> paoList;
    private FirebaseFirestore db;


    public PAOAdapter(List<PAOModel> paoList, FirebaseFirestore db) {
        this.paoList = paoList;
        this.db = db;

    private FirebaseFunctions firebaseFunctions;
    private FirebaseAuth firebaseAuth;
    private Context context;
    private CustomLoadingDialog customLoadingDialog;

    // Constructor to accept Firestore instance
    public PAOAdapter(List<PAOModel> paoList, FirebaseFirestore db, Activity context) {
        this.paoList = paoList;
        this.db = db;
        this.firebaseFunctions = FirebaseFunctions.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.context = context;
        customLoadingDialog = new CustomLoadingDialog(context);

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
        holder.tvDateAdded.setText(pao.getDateAdded());

        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(pao, position, holder.btnDelete, holder.itemView.getContext()));
    }

    private void showDeleteConfirmationDialog(PAOModel pao, int position, Button btnDelete, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PAO")
                .setMessage("Are you sure you want to delete this PAO?")
                .setPositiveButton("Yes", (dialog, which) -> deletePAO(pao, position, btnDelete, context))
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePAO(PAOModel pao, int position, Button btnDelete, Context context) {
        String paoId = pao.getDocumentId();  // Get the document ID from the PAO model
        if (paoId != null) {
            // Temporarily disable the button to prevent multiple clicks
            btnDelete.setEnabled(false);

            // Show a Toast indicating that deletion is in progress
            Toast.makeText(context, "Deleting PAO...", Toast.LENGTH_SHORT).show();

            // Proceed to delete the PAO from Firestore
            db.collection("users").document(paoId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Notify the user of success
                        Toast.makeText(context, "PAO DELETED ", Toast.LENGTH_SHORT).show();
                        // Remove the item from the local list
                        paoList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(context, "Error deleting PAO: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Optionally re-add the item to the list in case of failure
                        paoList.add(position, pao); // Re-add the PAO model
                        notifyItemInserted(position);
                    })
                    .addOnCompleteListener(task -> {
                        // Re-enable the button after the operation completes
                        btnDelete.setEnabled(true);
                    });
        } else {
            Toast.makeText(context, "PAO ID is null", Toast.LENGTH_SHORT).show();
                .setPositiveButton("Yes", (dialog, which) -> {
                    customLoadingDialog.showLoadingScreen();
                    deletePAO(paoId, position, context); // Proceed with delete after confirmation
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .show();
    }

    // Method to delete PAO from Firestore and update RecyclerView
    private void deletePAO(String paoId, int position, Context context) {
        // Check if user is signed in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "User must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ID token from the currently signed-in user
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();  // Retrieve the ID token
                        db.collection("users").document(paoId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // Prepare data to send to the Firebase function
                                        String uid = paoId;

                                        HashMap<String, Object> info = new HashMap<>();
                                        info.put("uid", uid);
                                        info.put("idToken", idToken);

                                        Log.d("PAOAdapter","Data is:"+info);
                                        // Call the Firebase function with the ID token
                                        firebaseFunctions
                                                .getHttpsCallable("deleteUserAccount")
                                                .call(info)
                                                .addOnCompleteListener(task1 -> {
                                                    customLoadingDialog.hideLoadingScreen();
                                                    if (task1.isSuccessful()) {
                                                        paoList.remove(position);
                                                        notifyItemRemoved(position); // Notify the adapter that the item was removed
                                                        Toast.makeText(context, "PAO deleted", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "Error Deleting User: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        // If the document doesn't exist
                                        Toast.makeText(context, "PAO document does not exist in Firestore", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else {
                        customLoadingDialog.hideLoadingScreen();
                        Log.e("Error", "Failed to get ID token: " + task.getException().getMessage());
                        Toast.makeText(context, "Failed to get ID token.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public int getItemCount() {
        return paoList.size();
    }

    static class PAOViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaoName, tvPaoEmail, tvDateAdded;
        Button btnDelete;

        PAOViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaoName = itemView.findViewById(R.id.tvPaoName);
            tvPaoEmail = itemView.findViewById(R.id.tvPaoEmail);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}