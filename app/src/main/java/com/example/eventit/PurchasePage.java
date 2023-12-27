package com.example.eventit;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PurchasePage extends AppCompatActivity {

    private static final String TAG = "PurchasePage";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventPriceTextView;
    private TextView eventLocationTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private NumberPicker ticketQuantityPicker;
    private TextView totalPriceTextView;
    private Button purchaseButton;
    private double eventPrice;
    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_page);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        //eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDateTextView = findViewById(R.id.event_date_text_view);
        //eventPriceTextView = findViewById(R.id.event_price_text_view);
        //eventLocationTextView = findViewById(R.id.event_location_text_view);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        ticketQuantityPicker = findViewById(R.id.ticket_quantity_picker);
        totalPriceTextView = findViewById(R.id.total_price_text_view);
        purchaseButton = findViewById(R.id.purchase_button);
        emailEditText = findViewById(R.id.email_edit_text);
        eventId = getIntent().getStringExtra("eventId");

        // Check if eventId is not null
        if (eventId != null) {
            db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Process event details
                        String eventName = document.getString("nazwa");
                        eventPrice = document.getDouble("cena");
                        String city = document.getString("miasto");
                        String street = document.getString("adres");
                        Date eventDate = document.getDate("data");
                        int tickets = document.getLong("bilety").intValue();
                        ticketQuantityPicker.setMaxValue(tickets);

                        //eventNameTextView.setText("Nazwa: " + eventName);
                        //eventPriceTextView.setText("Cena: " + eventPrice + " zł");

                        //SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd-MM-yyyy ");
                        //String formattedDate = dateFormat.format(eventDate);
                        //eventDateTextView.setText(formattedDate + ", " + street + ", " + city);

                        ticketQuantityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                            updateTotalPrice();
                        });
                    }
                }
            });

            // Retrieve user data based on user ID
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                db.collection("users").document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = document.getString("imie");
                                    String surname = document.getString("nazwisko");
                                    emailEditText.setText(currentUser.getEmail());
                                    firstNameEditText.setText(name);
                                    lastNameEditText.setText(surname);
                                }
                            }
                        });
            }
        }

        purchaseButton.setOnClickListener(view -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String userEmailShipping = emailEditText.getText().toString();

                if (isValidEmail(userEmailShipping)) {
                    int selectedQuantity = ticketQuantityPicker.getValue();
                    int availableTickets = ticketQuantityPicker.getMaxValue();

                    if (selectedQuantity <= availableTickets) {
                        int newAvailableTickets = availableTickets - selectedQuantity;
                        db.collection("events").document(eventId)
                                .update("bilety", newAvailableTickets)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Event tickets updated successfully
                                        updateAvailableTickets(newAvailableTickets);
                                        double total = selectedQuantity * eventPrice;
                                        createPurchaseHistoryDocument(userId, userEmailShipping, firstName, lastName, eventId, selectedQuantity, total);
                                        String message = "Purchase successful!\nTotal Price: " + totalPriceTextView.getText().toString();
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Failed to update event tickets
                                        Toast.makeText(this, "Failed to update event tickets", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Not enough tickets available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAvailableTickets(int newAvailableTickets) {
        ticketQuantityPicker.setMaxValue(newAvailableTickets);
        ticketQuantityPicker.setValue(1); // Reset ticket quantity to 1 after purchase
    }

    private void createPurchaseHistoryDocument(String userId, String userEmailShipping, String firstName, String lastName, String eventId, int quantity, double total) {
        Map<String, Object> purchaseData = new HashMap<>();
        purchaseData.put("id_uzytkownika", userId);
        purchaseData.put("email", userEmailShipping);
        purchaseData.put("imie", firstName);
        purchaseData.put("nazwisko", lastName);
        purchaseData.put("id_wydarzenia", eventId);
        purchaseData.put("ilosc_zakupionych_biletow", quantity);
        purchaseData.put("calkowita_cena", total);

        db.collection("purchase history")
                .add(purchaseData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Purchase history document added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding purchase history document", e));
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void updateTotalPrice() {
        int selectedQuantity = ticketQuantityPicker.getValue();
        double total = selectedQuantity * eventPrice;
        totalPriceTextView.setText("Cena końcowa: " + total + " zł");
    }
}
