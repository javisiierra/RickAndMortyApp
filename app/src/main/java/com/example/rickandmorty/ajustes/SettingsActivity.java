package com.example.rickandmorty.ajustes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.rickandmorty.MainActivity;
import com.example.rickandmorty.R;
import com.example.rickandmorty.login.LoginActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextPassword;
    private Button buttonChangePassword;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Verificar si el usuario está autenticado
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Si no hay usuario autenticado, redirigir a LoginActivity
            Toast.makeText(this, "Por favor, inicia sesión para acceder a la configuración.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Usuario autenticado, puedes usar currentUser.getUid() aquí
        String userId = currentUser.getUid();
        Toast.makeText(this, "Usuario autenticado: " + userId, Toast.LENGTH_SHORT).show();

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Inicializar vistas
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);


        buttonChangePassword.setOnClickListener(v -> {
            String currentPassword = ((EditText) findViewById(R.id.editTextCurrentPassword)).getText().toString().trim();
            String newPassword = editTextPassword.getText().toString().trim();

            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu contraseña actual", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty() || newPassword.length() < 6) {
                Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reautenticar al usuario
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear credenciales con el correo actual y la contraseña ingresada
            String email = user.getEmail();
            if (email == null) {
                Toast.makeText(this, "Error: No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Cambiar la contraseña después de reautenticarse
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show();
                }
            });
        });

        Button buttonReturnToMain = findViewById(R.id.buttonReturnToMain);
        buttonReturnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retorna a MainActivity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Opcional, cierra la actividad actual
            }
        });

        // Cerrar sesión
        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(view -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}