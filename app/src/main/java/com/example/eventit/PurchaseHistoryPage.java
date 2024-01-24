package com.example.eventit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseHistoryPage extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListView eventListView;
    private ArrayAdapter<View> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_history_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        eventListView = findViewById(R.id.event_list_view);
        adapter = new ArrayAdapter<View>(this, R.layout.purchase_history_list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return getItem(position);
            }
        };

        eventListView.setAdapter(adapter);
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            Task<Void> allTasks = db.collection("purchase history")
                    .whereEqualTo("id_uzytkownika", userId)
                    .get()
                    .continueWithTask(task -> {
                        List<Task<Void>> tasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getString("id_wydarzenia");

                            Task<Void> eventTask = db.collection("events").document(eventId)
                                    .get()
                                    .continueWith(eventDocument -> {
                                        if (eventDocument.isSuccessful()) {
                                            View purchaseHistoryItemView = getLayoutInflater().inflate(R.layout.purchase_history_list, null);

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

                                            TextView eventDetailsTextView = purchaseHistoryItemView.findViewById(R.id.eventDetailsTextView);
                                            eventDetailsTextView.setText("Nazwa: " + eventName +
                                                    "\nAdres: " + eventAddress + ", " + eventLocation +
                                                    "\nData: " + formattedDate +
                                                    "\nDane klienta: " + userName + " " + userSurname +
                                                    "\nCalkowita cena: " + totalPrice + " zł" +
                                                    "\nIlosc zakupionych biletow: " + quantity);

                                            ImageView eventImageView = purchaseHistoryItemView.findViewById(R.id.eventImageView);
                                            String imageUrl = eventDocument.getResult().getString("url");

                                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                                Picasso.get().load(imageUrl).placeholder(R.drawable.photo_not_found).into(eventImageView);
                                            }
                                            adapter.add(purchaseHistoryItemView);

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
