package com.example.eventit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventsPage extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDetailsTextView;
    private TextView eventTicketsTextView;

    private Button buyTicketsButton;
    private ImageView eventImageView;


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
        eventImageView = findViewById(R.id.event_image_view);

        String eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String eventName = document.getString("nazwa");
                        String eventDescription = document.getString("opis");
                        Double eventPrice = document.getDouble("cena");
                        Date eventDate = document.getDate("data");
                        String city = document.getString("miasto");
                        String street = document.getString("adres");
                        String imageUrl = document.getString("url");

                        eventNameTextView.setText(eventName);
                        eventDetailsTextView.setText(eventDescription);
                        eventTicketsTextView.setText("Kup bilet już za " + eventPrice + " zł");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd-MM-yyyy ");
                        String formattedDate = dateFormat.format(eventDate);
                        eventDateTextView.setText(formattedDate +  ", " + street + ", " + city);

                        Glide.with(this)
                                .load(imageUrl)
                                .into(eventImageView);

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
