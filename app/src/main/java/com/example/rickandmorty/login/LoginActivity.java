package com.example.rickandmorty.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rickandmorty.MainActivity;
import com.example.rickandmorty.R;
import com.example.rickandmorty.ajustes.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, forgotPasswordButton;
    private CheckBox checkBoxKeepLoggedIn;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Vincular vistas
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        forgotPasswordButton = findViewById(R.id.buttonForgotPassword);
        checkBoxKeepLoggedIn = findViewById(R.id.checkBoxKeepLoggedIn);

        // Verificar si el usuario está conectado
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean keepLoggedIn = sharedPreferences.getBoolean("KeepLoggedIn", false);

        if (keepLoggedIn && firebaseAuth.getCurrentUser() != null) {
            // Ir directamente a MainActivity si KeepLoggedIn está activado
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Iniciar sesión
        buttonLogin.setOnClickListener(view -> loginUser());

        // Registrar un nuevo usuario
        buttonRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Recuperar contraseña
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Guardar el estado del checkbox en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("KeepLoggedIn", checkBoxKeepLoggedIn.isChecked());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Verificar si el checkbox está desactivado y cerrar sesión
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean keepLoggedIn = sharedPreferences.getBoolean("KeepLoggedIn", false);

        if (!keepLoggedIn && firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
    }

}