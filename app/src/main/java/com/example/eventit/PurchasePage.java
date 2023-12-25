package com.example.eventit;

import android.os.Bundle;
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

public class PurchasePage extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventPriceTextView;
    private TextView eventLocationTextView;
    private EditText firstNameEditText; // Change to EditText for first name
    private EditText lastNameEditText; // Change to EditText for last name
    private EditText emailEditText;
    private NumberPicker ticketQuantityPicker; // NumberPicker for ticket quantity
    private TextView totalPriceTextView; // Added TextView for displaying total price
    private Button purchaseButton; // Added Button for the purchase
    private double eventPrice; // Added variable to store event price

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_page);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDateTextView = findViewById(R.id.event_date_text_view);
        eventPriceTextView = findViewById(R.id.event_price_text_view);
        eventLocationTextView = findViewById(R.id.event_location_text_view);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        ticketQuantityPicker = findViewById(R.id.ticket_quantity_picker); // Initialize NumberPicker
        totalPriceTextView = findViewById(R.id.total_price_text_view); // Initialize total price TextView
        purchaseButton = findViewById(R.id.purchase_button); // Initialize purchase button
        emailEditText = findViewById(R.id.email_edit_text);

        // Retrieve event ID from the intent
        String eventId = getIntent().getStringExtra("eventId");

        // Check if eventId is not null
        if (eventId != null) {
            // Retrieve event details from Firestore
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

                        // Display event details on PurchasePage
                        eventNameTextView.setText("Nazwa wydarzenia: " + eventName);

                        // Display event price
                        eventPriceTextView.setText("Cena: " + eventPrice + " zł");

                        // Display event location
                        eventLocationTextView.setText("Adres: " + street + ", " + city);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        String formattedDate = dateFormat.format(eventDate);
                        eventDateTextView.setText(formattedDate + ", " + street + ", " + city);

                        // Set a listener for the ticketQuantityPicker to update total price when quantity changes
                        ticketQuantityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                            updateTotalPrice(); // Update total price when the ticket quantity changes
                        });
                    }
                }
            });

            // Retrieve user data based on email
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                if (userEmail != null) {
                    emailEditText.setText(userEmail);

                    db.collection("users").document(userEmail)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Retrieve user details from Firestore
                                        String name = document.getString("imie");
                                        String surname = document.getString("nazwisko");
                                        // Populate EditText fields with user details
                                        firstNameEditText.setText(name);
                                        lastNameEditText.setText(surname);
                                    }
                                }
                            });
                }
            }
        }

        // Set a click listener for the purchaseButton
        purchaseButton.setOnClickListener(view -> {
            // Implement the purchase logic here
            int selectedQuantity = ticketQuantityPicker.getValue();
            double total = selectedQuantity * eventPrice;
            String message = "Purchase button clicked!\nTotal Price: " + total + " zł";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    // Method to update the total price based on the selected ticket quantity
    private void updateTotalPrice() {
        int selectedQuantity = ticketQuantityPicker.getValue();
        double total = selectedQuantity * eventPrice;
        totalPriceTextView.setText("Cena końcowa: " + total + " zł");
    }
}
