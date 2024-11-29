package com.example.wejeep;

import android.app.AlertDialog;
import android.app.Activity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.List;


public class PAOAdapter extends RecyclerView.Adapter<PAOAdapter.PAOViewHolder> {

    private List<PAOModel> paoList;
    private FirebaseFirestore db;
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
        holder.tvDateAdded.setText(pao.getDateAdded()); // Set the date added

        // Conditionally hide the delete button (e.g., based on role or other logic)
        boolean showDeleteButton = shouldShowDeleteButton(); // Replace with your logic
        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE); // Show the button
            holder.btnDelete.setOnClickListener(v -> {
                showDeleteConfirmationDialog(pao.getDocumentId(), position, holder.itemView.getContext());
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE); // Hide the button
        }
    }

    // Mock method to decide whether to show the delete button
    private boolean shouldShowDeleteButton() {
        // Example: Only allow admin users to see the delete button
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null && currentUser.getEmail().equals("admin@example.com");
    }

    // Method to show a confirmation dialog before deleting
    private void showDeleteConfirmationDialog(String paoId, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PAO")
                .setMessage("Are you sure you want to delete this PAO?")
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