package com.example.eventit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventsPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private int availableTickets; // Variable to store the available tickets
    private int selectedTickets; // Variable to store the selected number of tickets

    private TextView eventNameTextView;
    private TextView eventDetailsTextView;
    private TextView eventTicketsTextView;
    private NumberPicker ticketNumberPicker;
    private Button buyTicketsButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_page);

        db = FirebaseFirestore.getInstance();

        eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDetailsTextView = findViewById(R.id.event_details_text_view);
        eventTicketsTextView = findViewById(R.id.event_tickets_text_view);
        ticketNumberPicker = findViewById(R.id.ticket_number_picker);
        buyTicketsButton = findViewById(R.id.buy_tickets_button);

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
                        String eventDescription = document.getString("opis");
                        Double eventPrice = document.getDouble("cena");
                        availableTickets = document.getLong("bilety").intValue(); // Get available tickets

                        // Display event details
                        eventNameTextView.setText(eventName);
                        eventDetailsTextView.setText("Dowiedz sie więcej o wydarzeniu... \n" + eventDescription);
                        eventTicketsTextView.setText("Kup bilet juz za " + eventPrice + " zł");

                        // Set up NumberPicker
                        ticketNumberPicker.setMinValue(1);
                        ticketNumberPicker.setMaxValue(availableTickets);
                        ticketNumberPicker.setValue(1);

                        // Set up Button click listener
                        buyTicketsButton.setOnClickListener(view -> {
                            // Get the selected number of tickets
                            selectedTickets = ticketNumberPicker.getValue();

                            // Handle the ticket purchase logic
                            if (0 < availableTickets) {
                                // Perform ticket purchase
                                // You can add your logic here, e.g., update Firestore with the new ticket count
                                Toast.makeText(this, "Bilety zakupione!", Toast.LENGTH_SHORT).show();

                                // Update available tickets and adjust NumberPicker max value
                                availableTickets -= selectedTickets;
                                ticketNumberPicker.setMaxValue(availableTickets);
                                ticketNumberPicker.setValue(1); // Reset NumberPicker to 1
                            } else {
                                Toast.makeText(this, "Nie wystarczająca ilość dostępnych biletów.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Handle the case where the document does not exist
                    }
                } else {
                    // Handle exceptions or errors
                }
            });
        }
    }
}
