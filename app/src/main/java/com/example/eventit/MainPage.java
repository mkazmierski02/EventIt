package com.example.eventit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        mAuth = FirebaseAuth.getInstance();

        // Inicjalizacja przycisku
        Button logoutButton = findViewById(R.id.logout_button);

        // Inicjalizacja ListView
        ListView eventTypesListView = findViewById(R.id.event_types);

        // Inicjalizacja klienta GoogleSignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // Pobranie danych z tablicy zasobów
        String[] eventTypes = getResources().getStringArray(R.array.event_types);

        // Ustawienie danych dla ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, eventTypes);
        eventTypesListView.setAdapter(adapter);

        // Dodanie obsługi kliknięcia na element z listy
        eventTypesListView.setOnItemClickListener((parent, view, position, id) -> {
            // Obsługa kliknięcia na element z listy
            String selectedEventType = eventTypes[position];
            Toast.makeText(MainPage.this, "Wybrano: " + selectedEventType, Toast.LENGTH_SHORT).show();

            // Przygotowanie Intentu dla nowej aktywności (EventsPage)
            Intent eventsIntent = new Intent(MainPage.this, EventsPage.class);

            // Przekazanie nazwy wybranego wydarzenia do nowej aktywności
            eventsIntent.putExtra("event_category", selectedEventType);

            // Uruchomienie nowej aktywności
            startActivity(eventsIntent);
        });

        // Dodanie obsługi kliknięcia przycisku
        logoutButton.setOnClickListener(view -> {
            // Wylogowanie użytkownika z Firebase
            mAuth.signOut();

            // Wylogowanie użytkownika z konta Google
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        // Przekierowanie do MainActivity
                        Intent intent = new Intent(MainPage.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Opcjonalne: Zamknij aktualną aktywność, aby użytkownik nie mógł wrócić przyciskiem "Wstecz"
                    });
        });
    }
}
