package com.example.eventit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventsPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private int availableTickets; // Variable to store the available tickets

    private TextView eventNameTextView;
    private TextView eventDetailsTextView;
    private TextView eventTicketsTextView;

    private Button buyTicketsButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_page);

        db = FirebaseFirestore.getInstance();

        eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDetailsTextView = findViewById(R.id.event_details_text_view);
        eventTicketsTextView = findViewById(R.id.event_tickets_text_view);
        buyTicketsButton = findViewById(R.id.buy_tickets_button);
        TextView eventDateTextView = findViewById(R.id.event_date_text_view);


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
                        Date eventDate = document.getDate("data");// Get event date
                        String city = document.getString("miasto");
                        String street = document.getString("adres");

                        // Display event details
                        eventNameTextView.setText(eventName);
                        eventDetailsTextView.setText("Dowiedz się więcej o wydarzeniu... \n" + eventDescription);
                        eventTicketsTextView.setText("Kup bilet już za " + eventPrice + " zł");

                        // Display event date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        String formattedDate = dateFormat.format(eventDate);
                        eventDateTextView.setText(formattedDate +  ", " + street + ", " + city);


                        buyTicketsButton.setOnClickListener(view -> {

                            Intent intent = new Intent(EventsPage.this, PurchasePage.class);
                            intent.putExtra("eventId", eventId);
                            startActivity(intent);
                        });
                    }
                }
            });
        }
    }
}
