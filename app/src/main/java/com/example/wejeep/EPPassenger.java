package com.example.wejeep;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.atomic.AtomicInteger;

public class EPPassenger extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private CustomLoadingDialog customLoadingDialog;
    Button btnBackEPP, btnApplyChangesEPP;
    ImageView ivProfilePicturePP;
    TextInputEditText etNewNameEPP, etNewPasswordEPP;
    DrawerLayout drawerLayout;

    // Firebase instances
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    // Store original values
    private String originalName;
    private String originalProfilePicture; // Store the original profile picture URL
    private Uri imageUri; // Store the image URI
    private boolean isProfilePictureUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eppassenger);

        customLoadingDialog = new CustomLoadingDialog(this);

        btnBackEPP = findViewById(R.id.btnBackEPP);
        btnApplyChangesEPP = findViewById(R.id.btnApplyChangesEPP);
        ivProfilePicturePP = findViewById(R.id.ivProfilePicturePP);
        etNewNameEPP = findViewById(R.id.etNewNameEPP);
        etNewPasswordEPP = findViewById(R.id.etNewPasswordEPP);
        drawerLayout = findViewById(R.id.drawer_layout);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        currentUser = auth.getCurrentUser();
        storageReference = storage.getReference("profile_pictures");

        // Load current user data
        loadCurrentUserData();

        btnBackEPP.setOnClickListener(view -> {
            startActivity(new Intent(EPPassenger.this, PPassenger.class));
        });

        ivProfilePicturePP.setOnClickListener(view -> openFileChooser());

        btnApplyChangesEPP.setOnClickListener(view -> applyChanges());
    }

    private void loadCurrentUserData() {
        if (currentUser != null) {
            // Fetch user data from Firestore
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Get the original name and profile picture from Firestore
                    originalName = task.getResult().getString("name");
                    originalProfilePicture = task.getResult().getString("profilePicture");
                    etNewNameEPP.setText(originalName);

                    // Load profile picture using Glide
                    loadProfilePicture(originalProfilePicture);
                } else {
                    Log.e("EPPassenger", "Error fetching user data: ", task.getException());
                }
            });
        }
    }

    private void loadProfilePicture(String profilePicture) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Load as URL using Glide
            Glide.with(EPPassenger.this)
                    .load(profilePicture) // Assuming this is a URL
                    .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                    .error(R.drawable.error_image) // Image to show on error
                    .into(ivProfilePicturePP);
        } else {
            // Load placeholder if no image
            Glide.with(this)
                    .load(R.drawable.placeholder_image)
                    .into(ivProfilePicturePP);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Store the URI of the selected image
            ivProfilePicturePP.setImageURI(imageUri); // Preview the selected image
            isProfilePictureUpdated = true; // Mark as updated
        }
    }
    AtomicInteger pendingUpdates = new AtomicInteger(); // Counter for pending updates
    private void applyChanges() {
        String newName = etNewNameEPP.getText().toString().trim();
        String newPassword = etNewPasswordEPP.getText().toString().trim();

        // Check if any changes have been made
        boolean isNameChanged = !newName.equals(originalName);
        boolean isPasswordChanged = !newPassword.isEmpty();
        boolean isProfilePictureChanged = isProfilePictureUpdated;

        if (!isNameChanged && !isPasswordChanged && !isProfilePictureChanged) {
            Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
            return; // Exit the method if no changes
        }

        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());



            // Update the name if changed
            if (isNameChanged) {
                // Validate name
                if (newName.length() <2 || !newName.matches("[a-zA-Z ]+")) {
                    Toast.makeText(this, "Name must be at least 2 characters and contain only letters", Toast.LENGTH_SHORT).show();
                    return;
                }
                pendingUpdates.getAndIncrement(); // Increment counter
                userRef.update("name", newName);
                // Also update the display name in Firebase Authentication
                currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("EPPassenger", "Display name updated in Firebase Authentication.");
                    } else {
                        Log.e("EPPassenger", "Error updating display name in Firebase Authentication: ", task.getException());
                    }
                    pendingUpdates.getAndDecrement(); // Decrement counter
                    checkForCompletion(pendingUpdates.get()); // Check if all updates are done
                });
            }

            // Update the password only if it's changed
            if (isPasswordChanged) {
                if (newPassword.length() < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }
                pendingUpdates.getAndIncrement(); // Increment counter
                currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("EPPassenger", "Password updated in Firebase Authentication.");
                    } else {
                        Log.e("EPPassenger", "Error updating password in Firebase Authentication: ", task.getException());
                    }
                    pendingUpdates.getAndDecrement(); // Decrement counter
                    checkForCompletion(pendingUpdates.get()); // Check if all updates are done
                });
            }

            // Update the profile picture if changed
            if (isProfilePictureChanged) {
                uploadProfilePicture(); // Upload the image to Firebase Storage
                customLoadingDialog.showLoadingScreen();
            } else {
                loadCurrentUserData(); // Reload user data if no picture change
            }
            // Check if all updates are done
            checkForCompletion(pendingUpdates.get()); // Initial check
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePicture() {
        if (imageUri != null) {
            pendingUpdates.getAndIncrement(); // Increment counter
            StorageReference fileReference = storageReference.child(currentUser.getUid() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String profilePictureUrl = downloadUri.toString();

                            // Update the profile picture URL in Firestore
                            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
                            userRef.update("profilePicture", profilePictureUrl)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Update the profile picture in Firebase Authentication
                                            updateFirebaseAuthProfilePicture(profilePictureUrl);
                                            customLoadingDialog.hideLoadingScreen();
                                        } else {
                                            Toast.makeText(EPPassenger.this, "Error updating profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                        pendingUpdates.getAndDecrement(); // Decrement counter
                                        checkForCompletion(pendingUpdates.get()); // Initial check
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EPPassenger", "Error uploading profile picture: ", e);
                        Toast.makeText(EPPassenger.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("EPPassenger", "Image URI is null.");
            Toast.makeText(EPPassenger.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirebaseAuthProfilePicture(String profilePictureUrl) {
        pendingUpdates.getAndIncrement(); // Increment counter
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(profilePictureUrl))
                .build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Toast.makeText(EPPassenger.this, "Profile picture updated", Toast.LENGTH_SHORT).show();  removed toast, replaced with changes applied
                        loadCurrentUserData(); // Reload user data to reflect changes
                    } else {
                        Log.e("EPPassenger", "Error updating profile picture in Firebase Authentication: ", task.getException());
                    }
                    pendingUpdates.getAndDecrement(); // Decrement counter
                    checkForCompletion(pendingUpdates.get()); // Initial check
                });
    }
    // Method to check if all updates are completed
    private void checkForCompletion(int pendingUpdates) {
        if (pendingUpdates <= 0) {
            Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EPPassenger.this, PPassenger.class));
        }
    }

}
