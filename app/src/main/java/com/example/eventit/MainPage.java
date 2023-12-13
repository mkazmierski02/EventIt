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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> allEvents;
    private List<String> displayedEvents;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        initFirebase();
        initViews();
        readEventsFromFirestore();
        setupLogoutButton();
        setupSearchEditText();
        setupSortSpinner();
        setupCategorySpinner();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        ListView eventListView = findViewById(R.id.event_list_view);
        Button logoutButton = findViewById(R.id.logout_button);
        allEvents = new ArrayList<>();
        displayedEvents = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedEvents);
        eventListView.setAdapter(adapter);
    }

    private void readEventsFromFirestore() {
        CollectionReference eventsRef = db.collection("events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allEvents.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String eventName = document.getString("nazwa");
                    //String eventDescription = document.getString("opis");
                    String eventCategory = document.getString("kategoria");
                    Timestamp timestamp = document.getTimestamp("data");
                    Date date = (timestamp != null) ? timestamp.toDate() : null;
                    String eventDate = (date != null) ? formatDate(date) : "";
                    Double eventPrice = document.getDouble("cena");
                    String priceString = (eventPrice != null) ? String.valueOf(eventPrice) : "";

                    String eventString = "Nazwa: " + eventName + "\nCena: " + priceString + "\nData: " + eventDate + "\nKategoria: " + eventCategory;
                    allEvents.add(eventString);
                }

                filterEvents("");
            } else {
            }
        });
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void setupLogoutButton() {
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(MainPage.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupSearchEditText() {
        EditText searchEditText = findViewById(R.id.search_edit_text);
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
    }

    private void setupSortSpinner() {
        Spinner sortSpinner = findViewById(R.id.sort_spinner);
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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupCategorySpinner() {
        Spinner categorySpinner = findViewById(R.id.category_spinner);
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

    private void filterEvents(String searchText) {
        displayedEvents.clear();

        for (String event : allEvents) {
            if (event.toLowerCase().contains(searchText.toLowerCase())) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void filterEventsByCategory(String selectedCategory) {
        displayedEvents.clear();

        for (String event : allEvents) {
            if (selectedCategory.equals("Wszystkie") || event.toLowerCase().contains(selectedCategory.toLowerCase())) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void sortByDate() {
        Collections.sort(displayedEvents, (event1, event2) -> {
            String date1 = event1.split("\n")[2].replace("Data: ", "").trim();
            String date2 = event2.split("\n")[2].replace("Data: ", "").trim();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            try {
                Date dateTime1 = sdf.parse(date1);
                Date dateTime2 = sdf.parse(date2);

                return dateTime1.compareTo(dateTime2);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void sortByPriceAscending() {
        Collections.sort(displayedEvents, (event1, event2) -> {
            double price1 = Double.parseDouble(event1.split("\n")[1].replace("Cena:", "").trim());
            double price2 = Double.parseDouble(event2.split("\n")[1].replace("Cena:", "").trim());
            return Double.compare(price1, price2);
        });

        adapter.notifyDataSetChanged();
    }

    private void sortByPriceDescending() {
        Collections.sort(displayedEvents, (event1, event2) -> {
            double price1 = Double.parseDouble(event1.split("\n")[1].replace("Cena:", "").trim());
            double price2 = Double.parseDouble(event2.split("\n")[1].replace("Cena:", "").trim());
            return Double.compare(price2, price1);
        });

        adapter.notifyDataSetChanged();
    }
}
