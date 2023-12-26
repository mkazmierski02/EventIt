package com.example.eventit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userEmailTextView;
    private EditText editTextName;
    private EditText editTextSurname;
    private FirebaseFirestore db;
    private Button saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        mAuth = FirebaseAuth.getInstance();
        userEmailTextView = findViewById(R.id.user_email_text_view);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch the current user
        FirebaseUser user = mAuth.getCurrentUser();

        // Check if the user is not null
        if (user != null) {
            // Get the user's email
            String userEmail = user.getEmail();

            // Display the email in the TextView
            userEmailTextView.setText("Email: " + userEmail);

            // Retrieve user details from Firestore based on document ID (email)
            db.collection("users").document(user.getUid()) // Use UID as the document ID
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve user details from Firestore
                                String name = document.getString("imie");
                                String surname = document.getString("nazwisko");

                                // Populate EditText fields with user details
                                editTextName.setText(name);
                                editTextSurname.setText(surname);
                            }
                        }
                    });

            // Set a click listener for the Save button
            saveButton.setOnClickListener(v -> saveUserData(user.getUid()));
        }
    }

    private void saveUserData(String userId) {
        // Get updated user data from EditText fields
        String name = editTextName.getText().toString();
        String surname = editTextSurname.getText().toString();

        // Create or update user document in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("imie", name);
        userData.put("nazwisko", surname);

        db.collection("users").document(userId) // Use UID as the document ID
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Document successfully written
                    // You can add a Toast or other UI feedback here
                })
                .addOnFailureListener(e -> {
                    // Handle failures
                    // You can add a Toast or other UI feedback here
                });
    }
}
