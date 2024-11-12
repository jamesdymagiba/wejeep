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
    private FirebaseFirestore db;

    public PAOAdapter(List<PAOModel> paoList, FirebaseFirestore db) {
        this.paoList = paoList;
        this.db = db;
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
        }
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
