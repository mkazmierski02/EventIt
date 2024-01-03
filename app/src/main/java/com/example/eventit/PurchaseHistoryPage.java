package com.example.eventit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

            Task<Void> allTasks = db.collection("purchase history")
                    .whereEqualTo("id_uzytkownika", userId)
                    .get()
                    .continueWithTask(task -> {
                        List<Task<Void>> tasks = new ArrayList<>();

                        AtomicInteger position = new AtomicInteger(1);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getString("id_wydarzenia");

                            Task<Void> eventTask = db.collection("events").document(eventId)
                                    .get()
                                    .continueWith(eventDocument -> {
                                        if (eventDocument.isSuccessful()) {
                                            String eventName = eventDocument.getResult().getString("nazwa");
                                            String eventLocation = eventDocument.getResult().getString("miasto");
                                            Date eventDate = eventDocument.getResult().getDate("data");
                                            String eventAddress = eventDocument.getResult().getString("adres");

                                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd-MM-yyyy ");
                                            String formattedDate = dateFormat.format(eventDate);

                                            String userName = document.getString("imie");
                                            String userSurname = document.getString("nazwisko");
                                            double totalPrice = document.getDouble("calkowita_cena");
                                            int quantity = document.getLong("ilosc_zakupionych_biletow").intValue();

                                            String eventDetails = position + ".\n" + "Nazwa: " + eventName +
                                                    "\nAdres: " + eventAddress + ", " + eventLocation +
                                                    "\nData: " + formattedDate +
                                                    "\nDane klienta: " + userName + ", " + userSurname +
                                                    "\nCalkowita cena: " + totalPrice + " zł" +
                                                    "\nIlosc zakupionych biletow: " + quantity;

                                            adapter.add(eventDetails);
                                            position.getAndIncrement();
                                        }
                                        return null;
                                    });

                            tasks.add(eventTask);
                        }

                        return Tasks.whenAll(tasks);
                    });

        }
    }
}
