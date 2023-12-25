package com.example.eventit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userEmailTextView;
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextAddress;
    private EditText editTextPhoneNumber;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        mAuth = FirebaseAuth.getInstance();
        userEmailTextView = findViewById(R.id.user_email_text_view);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

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

            // Query Firestore to get user details based on email
            Query query = db.collection("users").whereEqualTo("email", userEmail);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Retrieve user details from Firestore
                        String name = document.getString("imie");
                        String surname = document.getString("nazwisko");
                        String address = document.getString("adres");
                        String phoneNumber = document.getString("telefon");

                        // Populate EditText fields with user details
                        editTextName.setText(name);
                        editTextSurname.setText(surname);
                        editTextAddress.setText(address);
                        editTextPhoneNumber.setText(phoneNumber);
                    }
                }
            });
        }
    }
}
