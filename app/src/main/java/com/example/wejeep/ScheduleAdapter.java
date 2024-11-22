package com.example.wejeep;

import android.app.AlertDialog;
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

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private ArrayList<ScheduleModel> scheduleList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ScheduleAdapter(ArrayList<ScheduleModel> scheduleList) { this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_card_view, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleModel schedule = scheduleList.get(position);
        Log.d("AdapterBinding", "Binding Schedule at position: " + position); // debug check of binding is working
        holder.tvFromDay.setText(schedule.getFromDay());
        holder.tvToDay.setText(schedule.getToDay());
        holder.tvFromTime.setText(schedule.getFromTime());
        holder.tvToTime.setText(schedule.getToTime());
        holder.tvSchedule.setText(schedule.getSchedule());

        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminEditSchedule.class);
            intent.putExtra("documentId", schedule.getDocumentId()); // Pass the document ID
            intent.putExtra("fromDay", schedule.getFromDay()); // Pass driver's name
            intent.putExtra("toDay", schedule.getToDay()); // Pass driver's contact
            intent.putExtra("fromTime" , schedule.getFromTime());
            intent.putExtra("toTime" , schedule.getToTime());
            intent.putExtra("Schedule", schedule.getSchedule());
            ((AdminManageScheduleScreen) holder.itemView.getContext()).startActivityForResult(intent, 100); // Start for result with a request code
        });


        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(schedule.getDocumentId(), position, holder.itemView.getContext()); // Show confirmation dialog
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvFromDay, tvToDay, tvFromTime, tvToTime, tvSchedule;
        Button btnEdit, btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFromDay = itemView.findViewById(R.id.tvFromDay);
            tvToDay = itemView.findViewById(R.id.tvToDay);
            tvFromTime = itemView.findViewById(R.id.tvFromTime);
            tvToTime = itemView.findViewById(R.id.tvToTime);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showDeleteConfirmationDialog(String scheduleId, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteSchedule(scheduleId, position, context); // Proceed with delete
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .show();
    }

    // Method to delete driver from Firestore and update RecyclerView
    private void deleteSchedule(String scheduleId, int position, Context context) {
        if (scheduleId != null) {
            db.collection("schedules").document(scheduleId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the unit from the list
                        scheduleList.remove(position);
                        // Notify the adapter about the removed item
                        notifyItemRemoved(position);
                        // Notify about item change in case of position changes
                        notifyItemRangeChanged(position, scheduleList.size());
                        Toast.makeText(context, "Schedule deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting schedule", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Schedule ID is null", Toast.LENGTH_SHORT).show();
        }
    }
}
