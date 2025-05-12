package com.example.rickandmorty.favoritos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rickandmorty.MainActivity;
import com.example.rickandmorty.R;

import com.example.rickandmorty.modelo.CharacterAdapter;
import com.example.rickandmorty.notificaciones.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.rickandmorty.modelo.Character;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListaFavoritos extends AppCompatActivity {
    private ListView listView;
    private CharacterAdapter adapter;
    private List<Character> favoriteCharacters = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        progressBar = findViewById(R.id.progressBarFavorites);
        progressBar.setVisibility(View.GONE); // Ocultarlo por defecto

        Button backButton = findViewById(R.id.buttonBackToMain);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ListaFavoritos.this, MainActivity.class);
            startActivity(intent);
            finish(); // Opcional: Finaliza la actividad actual
        });

        listView = findViewById(R.id.listViewFavorites);
        adapter = new CharacterAdapter(this, favoriteCharacters, true);
        listView.setAdapter(adapter);


        loadFavorites();
    }

    private void loadFavorites() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("Firestore", "Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();

        // Cambié la colección para que apunte a users/{userId}/favorites
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> characterIds = new ArrayList<>();
                    favoriteCharacters.clear(); // Limpia la lista antes de agregar nuevos datos

                    // Verificamos que la respuesta tenga documentos
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.e("Firestore", "No se encontraron favoritos.");
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Logs para ver qué datos estamos recuperando
                        Log.d("Firestore", "Documento recuperado: " + document.getId() + " -> " + document.getData());

                        // Manejo del campo 'id'
                        Object idObj = document.get("id");
                        String id = idObj != null ? idObj.toString() : "Desconocido";
                        characterIds.add(id);

                        // Agregar a la lista de personajes
                        String name = document.getString("name");
                        String imageUrl = document.getString("imageUrl");

                        // Agregamos el personaje a la lista
                        favoriteCharacters.add(new Character(id, name, imageUrl));
                    }

                    // Actualiza el adaptador
                    adapter.notifyDataSetChanged();

                    // Si necesitas información adicional de la API, llama a fetchCharactersFromApi
                    fetchCharactersFromApi(characterIds);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al cargar favoritos", e));
    }


    private void fetchCharactersFromApi(List<String> characterIds) {
        if (characterIds.isEmpty()) {
            Toast.makeText(this, "No hay personajes favoritos.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String ids = String.join(",", characterIds); // Une los IDs con coma
            String url = "https://rickandmortyapi.com/api/character/" + ids;

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Gson gson = new Gson();

                    // Cambiar de Character[] a un solo Character en caso de que la respuesta sea un objeto único
                    // La API puede devolver un solo personaje o varios personajes, por lo tanto, se debe verificar si es un arreglo o un objeto.
                    if (json.startsWith("[")) {
                        // Si es un arreglo, parseamos como un arreglo de personajes
                        Character[] characters = gson.fromJson(json, Character[].class);

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            favoriteCharacters.clear();
                            favoriteCharacters.addAll(Arrays.asList(characters));
                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        // Si es un objeto único, parseamos como un solo personaje
                        Character character = gson.fromJson(json, Character.class);

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            favoriteCharacters.clear();
                            favoriteCharacters.add(character);
                            adapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    runOnUiThread(() -> NotificationHelper.showError(this)); // Corregido aquí
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> NotificationHelper.showError(this)); // Corregido aquí también
            }
        }).start();
    }
}
