package com.example.eventit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText userEmailTextView;
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

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail();

            userEmailTextView.setText(userEmail);

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("imie");
                                String surname = document.getString("nazwisko");

                                editTextName.setText(name);
                                editTextSurname.setText(surname);
                            }
                        }
                    });
            saveButton.setOnClickListener(v -> saveUserData(user.getUid()));
        }
    }

    private void saveUserData(String userId) {
        String name = editTextName.getText().toString();
        String surname = editTextSurname.getText().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("imie", name);
        userData.put("nazwisko", surname);

        db.collection("users").document(userId)
                .set(userData).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dane zostały zaktualizowane.", Toast.LENGTH_SHORT).show();
                });

    }
}
