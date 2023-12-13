package com.example.eventit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class EventsPage extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_page);

        // Inicjalizacja Firestore
        db = FirebaseFirestore.getInstance();

        // Odczytanie wybranej kategorii z Inten

        // Pobranie i wyświetlenie wydarzeń z Firestore dla wybranej kategorii
        readEventsFromFirestore();
    }

    private void readEventsFromFirestore() {
        // Pobranie referencji do kolekcji "events"
        CollectionReference eventsRef = db.collection("events");

        // Pobranie wszystkich danych z Firestore (bez filtrowania)
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Odczytanie dokumentów z zapytania
                List<String> eventList = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Odczytanie danych z dokumentu
                    String eventName = document.getString("nazwa");
                    String eventDescription = document.getString("opis");

                    // Dodanie danych do listy
                    eventList.add("Nazwa: " + eventName + "\nOpis: " + eventDescription + "\n");
                }

                // Wyświetlenie danych w ListView
                displayEventData(eventList);
            } else {
                // Obsługa błędów zapytania
            }
        });
    }

    private void displayEventData(List<String> eventList) {
        // Inicjalizacja ListView
        ListView eventListView = findViewById(R.id.event_list_view);

        // Ustawienie danych dla ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, eventList);
        eventListView.setAdapter(adapter);
    }
}
