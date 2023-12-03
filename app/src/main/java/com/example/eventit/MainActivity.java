package com.example.eventit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123;

    private GoogleApiClient mGoogleApiClient;
    private Button googleLoginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        googleLoginButton = findViewById(R.id.google_login_button);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserWithEmailAndPassword();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUserWithEmailAndPassword();
            }
        });
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Pomyślnie zalogowano do Firebase za pomocą konta Google
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Tutaj możesz dodać nowe konto w Firebase Authentication
                            String email = user.getEmail();
                            String uid = user.getUid();

                            // Przykład dodawania nowego konta w Firebase (tutaj używając Firebase Realtime Database)
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            User newUser = new User(uid, email);
                            databaseReference.child(uid).setValue(newUser);

                            // Po zalogowaniu, otwórz nową aktywność (MainPageActivity)
                            openMainPage();
                        } else {
                            // Błąd logowania
                            Toast.makeText(MainActivity.this, "Błąd logowania", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Funkcja otwierająca nową aktywność (MainPage)
    private void openMainPage() {
        Intent intent = new Intent(MainActivity.this, MainPage.class);
        startActivity(intent);
        finish(); // Opcjonalne: Zamknij aktualną aktywność, aby użytkownik nie mógł wrócić przyciskiem "Wstecz"
    }

    private void loginUserWithEmailAndPassword() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Tutaj możesz przekierować użytkownika na ekran główny lub inny
                            openMainPage();
                        } else {
                            Log.e("TAG", "Błąd logowania: " + Objects.requireNonNull(task.getException()).getMessage());
                            Toast.makeText(MainActivity.this, "Błąd logowania", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void registerUserWithEmailAndPassword() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Tutaj możesz przekierować użytkownika na ekran główny lub inny
                        } else {
                            Log.e("TAG", "Błąd rejestracji: " + task.getException().getMessage());
                            Toast.makeText(MainActivity.this, "Błąd rejestracji", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
