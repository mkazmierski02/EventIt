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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> allEvents;
    private List<String> displayedEvents;

    private List<String> eventIds;
    private ArrayAdapter<String> adapter;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        ImageView logoutButton = findViewById(R.id.logout_button);

        ImageView accountIcon = findViewById(R.id.account);

        ImageView ticketIcon = findViewById(R.id.ticket);


        ListView eventListView = findViewById(R.id.event_list_view);

        EditText searchEditText = findViewById(R.id.search_edit_text);

        Spinner sortSpinner = findViewById(R.id.sort_spinner);

        Spinner categorySpinner = findViewById(R.id.category_spinner);


        readEventsFromFirestore();

        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        Intent intent = new Intent(MainPage.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
        });

        accountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, UserPage.class);
                startActivity(intent);
            }
        });
        ticketIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Przekieruj użytkownika do PurchaseHistoryPage
                Intent intent = new Intent(MainPage.this, PurchaseHistoryPage.class);
                startActivity(intent);
            }
        });


        allEvents = new ArrayList<>();
        displayedEvents = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, displayedEvents);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < eventIds.size()) {
                String selectedEventId = eventIds.get(position);
                Intent intent = new Intent(MainPage.this, EventsPage.class);
                intent.putExtra("eventId", selectedEventId);
                startActivity(intent);
            }
        });



        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterEvents(editable.toString());
            }
        });

        String[] sortOptions = {"Sortowanie: Brak", "Sortowanie chronologicznie", "Sortowanie od najtańszych", "Sortowanie od najdroższych"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSortOption = sortOptions[position];

                if (selectedSortOption.equals("Sortowanie chronologicznie")) {
                    sortByDate();
                } else if (selectedSortOption.equals("Sortowanie od najtańszych")) {
                    sortByPriceAscending();
                } else if (selectedSortOption.equals("Sortowanie od najdroższych")) {
                    sortByPriceDescending();
                } else if (selectedSortOption.equals("Sortowanie: Brak")) {
                    // Dodaj kod do zresetowania sortowania i wrócenia do pierwotnego wyświetlania
                    resetSorting();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        String[] categoryOptions = {"Kategoria: Wszystkie", "Kategoria: Sport", "Kategoria: Muzyka", "Kategoria: Sztuka", "Kategoria: Impreza", "Kategoria: Jedzenie", "Kategoria: Inne"};

        ArrayAdapter<String> categorySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedCategory = categoryOptions[position];

                filterEventsByCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void readEventsFromFirestore() {
        CollectionReference eventsRef = db.collection("events");
        eventIds = new ArrayList<>();
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allEvents.clear();
                displayedEvents.clear();
                eventIds.clear();

                Date currentDate = new Date(); // Aktualna data

                for (QueryDocumentSnapshot document : task.getResult()) {
                    int ticketsAvailable = document.getLong("bilety").intValue();

                    // Sprawdzenie, czy dostępne są bilety i data jest przyszła
                    if (ticketsAvailable > 0 && isFutureDate(document)) {
                        String eventId = document.getId();
                        eventIds.add(eventId);
                        String eventName = document.getString("nazwa");
                        String eventDescription = document.getString("opis");
                        String eventCategory = document.getString("kategoria");
                        String city = document.getString("miasto");
                        String street = document.getString("adres");

                        Timestamp timestamp = document.getTimestamp("data");
                        Date date = (timestamp != null) ? timestamp.toDate() : null;
                        String eventDate = (date != null) ? formatDate(date) : "";

                        Double eventPrice = document.getDouble("cena");

                        String priceString = (eventPrice != null) ? String.valueOf(eventPrice) : "";

                        String eventString = "Nazwa: " + eventName + "\nCena: " + priceString + "\nData: " + eventDate + "\nAdres: " + street + ", " + city + "\nKategoria: " + eventCategory;
                        allEvents.add(eventString);
                    }
                }

                // Zainicjowanie displayedEvents wszystkimi wydarzeniami
                displayedEvents.addAll(allEvents);

                filterEvents("");
            }
        });
    }

    private boolean isFutureDate(QueryDocumentSnapshot document) {
        Timestamp timestamp = document.getTimestamp("data");
        Date eventDate = (timestamp != null) ? timestamp.toDate() : null;
        return (eventDate != null && eventDate.after(new Date()));
    }
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void filterEvents(String searchText) {
        displayedEvents.clear();

        for (String event : allEvents) {
            String eventName = event.split("\n")[0].replace("Nazwa: ", "").trim();

            if (eventName.toLowerCase().contains(searchText.toLowerCase())) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void filterEventsByCategory(String selectedCategory) {
        displayedEvents.clear();

        for (String event : allEvents) {
            // Dodaj wszystkie wydarzenia, gdy wybrana kategoria to "Wszystkie"
            if (selectedCategory.equals("Kategoria: Wszystkie") || event.toLowerCase().contains(selectedCategory.toLowerCase())) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }


    private void sortByDate() {
        Collections.sort(displayedEvents, new Comparator<String>() {
            @Override
            public int compare(String event1, String event2) {

                String date1 = event1.split("\n")[2].replace("Data: ", "").trim();
                String date2 = event2.split("\n")[2].replace("Data: ", "").trim();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                try {
                    Date dateTime1 = sdf.parse(date1);
                    Date dateTime2 = sdf.parse(date2);

                    // Compare dates
                    assert dateTime1 != null;
                    return dateTime1.compareTo(dateTime2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void sortByPriceAscending() {
        Collections.sort(displayedEvents, new Comparator<String>() {
            @Override
            public int compare(String event1, String event2) {
                double price1 = Double.parseDouble(event1.split("\n")[1].replace("Cena:", "").trim());
                double price2 = Double.parseDouble(event2.split("\n")[1].replace("Cena:", "").trim());
                return Double.compare(price1, price2);
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void sortByPriceDescending() {
        Collections.sort(displayedEvents, new Comparator<String>() {
            @Override
            public int compare(String event1, String event2) {
                double price1 = Double.parseDouble(event1.split("\n")[1].replace("Cena:", "").trim());
                double price2 = Double.parseDouble(event2.split("\n")[1].replace("Cena:", "").trim());
                return Double.compare(price2, price1);
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void resetSorting() {
        // Zresetuj sortowanie i wróć do pierwotnej listy wydarzeń
        displayedEvents.clear();
        displayedEvents.addAll(allEvents);
        adapter.notifyDataSetChanged();
    }

}