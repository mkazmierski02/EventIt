package com.example.eventit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> allEvents; // Store all events to reset the list when search is cleared
    private List<String> displayedEvents; // Store currently displayed events

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        // Inicjalizacja Firestore
        db = FirebaseFirestore.getInstance();

        // Inicjalizacja FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicjalizacja ListView
        ListView eventListView = findViewById(R.id.event_list_view);

        // Inicjalizacja przycisku wylogowania
        Button logoutButton = findViewById(R.id.logout_button);

        // Inicjalizacja EditText
        EditText searchEditText = findViewById(R.id.search_edit_text);

        // Inicjalizacja Spinner
        Spinner sortSpinner = findViewById(R.id.sort_spinner);

        // Pobranie i wyświetlenie wydarzeń z Firestore
        readEventsFromFirestore();

        // Dodanie obsługi kliknięcia przycisku wylogowania
        logoutButton.setOnClickListener(view -> {
            // Wylogowanie użytkownika z Firebase
            mAuth.signOut();

            // Przekierowanie do MainActivity
            Intent intent = new Intent(MainPage.this, MainActivity.class);
            startActivity(intent);
            finish(); // Opcjonalne: Zamknij aktualną aktywność, aby użytkownik nie mógł wrócić przyciskiem "Wstecz"
        });

        // Save all events initially
        allEvents = new ArrayList<>();
        displayedEvents = new ArrayList<>();

        // Add TextWatcher to the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Filter events based on the search input
                filterEvents(editable.toString());
            }
        });

        // Lista opcji sortowania
        String[] sortOptions = {"Sortuj po dacie", "Sortuj po cenie (od najtańszych)", "Sortuj po cenie (od najdroższych)"};

        // Adapter dla Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        // Dodanie obsługi wyboru sortowania
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // Obsługa wyboru sortowania
                String selectedSortOption = sortOptions[position];

                // Wywołaj funkcję sortowania w zależności od wybranej opcji

                if (selectedSortOption.equals("Sortowanie chronologicznie")) {
                    sortByDate();
                } else if (selectedSortOption.equals("Sortowanie od najtańszych")) {
                    sortByPriceAscending();
                } else if (selectedSortOption.equals("Sortowanie od najdroższych")) {
                    sortByPriceDescending();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Nie wymagane, ale możesz dodać obsługę
            }
        });
    }

    private void readEventsFromFirestore() {
        // Pobranie referencji do kolekcji "events"
        CollectionReference eventsRef = db.collection("events");

        // Pobranie wszystkich danych z Firestore (bez filtrowania)
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Odczytanie dokumentów z zapytania
                allEvents.clear(); // Clear the existing events

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Odczytanie danych z dokumentu
                    String eventName = document.getString("nazwa");
                    String eventDescription = document.getString("opis");

                    // Dodanie danych do listy
                    String eventString = "Nazwa: " + eventName + "\nOpis: " + eventDescription + "\n";
                    allEvents.add(eventString);
                }

                // Update the displayed events
                filterEvents(""); // Display all events initially
            } else {
                // Obsługa błędów zapytania
            }
        });
    }

    private void filterEvents(String searchText) {
        // Filter events based on the search input
        displayedEvents.clear();

        for (String event : allEvents) {
            // Sprawdź czy nazwa wydarzenia zawiera searchText
            String eventName = event.split("\n")[0]; // Zakładam, że nazwa jest pierwszym atrybutem
            if (eventName.toLowerCase().contains(searchText.toLowerCase())) {
                displayedEvents.add(event);
            }
        }

        // Update the displayed events
        displayEventData(displayedEvents);
    }

    // Implementuj poniższe funkcje sortujące zgodnie z własnymi potrzebami

    private void sortByDate() {
        // Implementuj sortowanie po dacie
    }

    private void sortByPriceAscending() {
        // Implementuj sortowanie po cenie od najtańszych
    }

    private void sortByPriceDescending() {
        // Implementuj sortowanie po cenie od najdroższych
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
