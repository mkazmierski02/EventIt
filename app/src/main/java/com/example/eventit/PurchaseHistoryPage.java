package com.example.eventit;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseHistoryPage extends AppCompatActivity {

    private static final String TAG = "PurchaseHistoryPage";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListView eventListView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_history_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eventListView = findViewById(R.id.event_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        eventListView.setAdapter(adapter);

        // Retrieve the current user
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("purchase history")
                    .whereEqualTo("id_uzytkownika", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int position = 1;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String eventId = document.getString("id_wydarzenia");
                                String userName = document.getString("imie");
                                String userSurname = document.getString("nazwisko");
                                double totalPrice = document.getDouble("calkowita_cena");
                                int quantity = document.getLong("ilosc_zakupionych_biletow").intValue();

                                int finalPosition = position;
                                db.collection("events").document(eventId)
                                        .get()
                                        .addOnSuccessListener(eventDocument -> {
                                            if (eventDocument.exists()) {
                                                String eventName = eventDocument.getString("nazwa");
                                                String eventLocation = eventDocument.getString("miasto");
                                                Date eventDate = eventDocument.getDate("data");
                                                String eventAddress = eventDocument.getString("adres");

                                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                                String formattedDate = dateFormat.format(eventDate);

                                                String eventDetails = finalPosition + ".\n" +"Nazwa: " + eventName +
                                                        "\nAdres: " + eventAddress +"," + eventLocation +
                                                        "\nData: " + formattedDate +
                                                        "\nImie: " + userName +
                                                        "\nNazwisko: " + userSurname +
                                                        "\nCalkowita cena: " + totalPrice +
                                                        "\nIlosc zakupionych biletow: " + quantity;
                                                adapter.add(eventDetails);
                                            } else {
                                                Log.d(TAG, "No such document");
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error getting event document", e));
                                position++;
                            }
                        } else {
                            Log.w(TAG, "Error getting purchase history documents.", task.getException());
                        }
                    });
        }
    }
}
