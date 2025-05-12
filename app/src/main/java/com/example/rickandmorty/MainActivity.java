package com.example.rickandmorty;

import static android.os.Build.VERSION_CODES.R;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rickandmorty.ajustes.SettingsActivity;
import com.example.rickandmorty.favoritos.ListaFavoritos;
import com.example.rickandmorty.login.LoginActivity;
import com.example.rickandmorty.modelo.Character;
import com.example.rickandmorty.modelo.CharacterAdapter;
import com.example.rickandmorty.notificaciones.NotificationHandler;
import com.example.rickandmorty.notificaciones.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  {

    private ListView listView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private CharacterAdapter adapter;
    private List<Character> characterList = new ArrayList<>();
    private NotificationHandler notificationHandler;
    private TextView textViewNoResults;
    private Button buttonAccount;
    private FirebaseAuth firebaseAuth;
    private NotificationHelper notificationHelper;
    private Button buttonFavoriteList;
    private boolean notificationSent = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Verificar y solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            101
                    );
                }
            }
        }

        // Inicialización del NotificationHandler
        notificationHandler = new NotificationHandler(this);
        // Inicializar NotificationHelper
        notificationHelper = new NotificationHelper(this);

        // Comprobar la última visita y enviar una notificación si es necesario
        checkLastVisitAndNotify();


        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Verifica si el usuario está autenticado
        if (firebaseAuth.getCurrentUser() == null) {
            // Redirige a LoginActivity si el usuario no está autenticado
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cierra MainActivity para evitar volver a esta pantalla sin autenticarse
            return;
        }

        // Vincular el botón en la Toolbar
        buttonAccount = findViewById(R.id.buttonLogin);

        // Actualizar el botón de la cuenta en la Toolbar
        updateToolbar();

        // Inicialización de vistas y adaptador
        listView = findViewById(R.id.listViewCharacters);
        progressBar = findViewById(R.id.progressBar);
        adapter = new CharacterAdapter(this, characterList, false);
        listView.setAdapter(adapter);
        textViewNoResults = findViewById(R.id.textViewNoResults);

        // Configuración del scroll infinito
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isLoading = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && !isLoading) {
                    isLoading = true;
                    fetchMoreCharacters(() -> isLoading = false); // Carga más personajes y libera el flag
                }
            }
        });

        buttonFavoriteList = findViewById(R.id.buttonFavoriteList);
        buttonFavoriteList.setOnClickListener(view -> {
            notificationHelper.notifyFavoriteCharacter(characterList);
            Intent intent = new Intent(MainActivity.this, ListaFavoritos.class);
            startActivity(intent);
        });

        // Descarga inicial de personajes
        fetchCharacters();
        if (!notificationSent) {
            notificationHelper.notifyDownloadComplete();
            notificationSent = true;
        }
        setupSearchView();
    }




    /**
     * Descarga los personajes desde la API y actualiza la interfaz de usuario.
     */
    private void fetchCharacters() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("https://rickandmortyapi.com/api/character").build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Character[] characters = gson.fromJson(
                            new JsonParser().parse(json).getAsJsonObject().get("results"),
                            Character[].class
                    );

                    runOnUiThread(() -> {
                        updateUI(Arrays.asList(characters)); // Actualiza la interfaz y lanza notificación
                    });
                } else {
                    showError();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError();
            }
        }).start();
    }

    private void fetchMoreCharacters(Runnable onComplete) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            // Usamos "page" para cargar más personajes según la paginación de la API
            String nextPageUrl = "https://rickandmortyapi.com/api/character?page=" + (characterList.size() / 20 + 1);
            Request request = new Request.Builder().url(nextPageUrl).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Character[] newCharacters = gson.fromJson(
                            new JsonParser().parse(json).getAsJsonObject().get("results"),
                            Character[].class
                    );

                    runOnUiThread(() -> {
                        characterList.addAll(Arrays.asList(newCharacters));
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        onComplete.run(); // Marca la tarea como completa
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    onComplete.run(); // Marca la tarea como completa incluso en caso de error
                });
            }
        }).start();
    }


    /**
     * Muestra un error si la descarga falla.
     */
    private void showError() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            notificationHelper.notifyDownloadError();
        });
    }

    /**
     * Actualiza la interfaz de usuario con la lista de personajes descargados.
     * Lanza una notificación indicando que la descarga ha finalizado.
     *
     * @param characters Lista de personajes descargados.
     */
    public void updateUI(List<Character> characters) {
        characterList.clear();
        characterList.addAll(characters);
        adapter.notifyDataSetChanged();
    }

    /**
     * Proporciona acceso al NotificationHandler.
     *
     * @return NotificationHandler.
     */
    public NotificationHandler getNotificationHandler() {
        return notificationHandler;
    }



    /**
     * Configura el SearchView y su listener
     */
    private void setupSearchView() {
        // Encuentra el Toolbar de la clase androidx.appcompat.widget.Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // Asegúrate que el id sea correcto

        // Establece el soporte de la toolbar para la actividad
        setSupportActionBar(toolbar);

        // Encuentra el SearchView dentro del Toolbar
        searchView = toolbar.findViewById(R.id.searchView); // Asegúrate que el SearchView tenga este id en el toolbar

        // Configura el listener del SearchView
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // El código del filtrado va aquí
                    if (query != null && !query.isEmpty()) {
                        List<Character> filteredList = new ArrayList<>();
                        for (Character character : characterList) {
                            if (character.getName().equalsIgnoreCase(query)) {  // Filtrado exacto
                                filteredList.add(character);
                            }
                        }

                        if (filteredList.isEmpty()) {
                            // Mostrar el mensaje si no se encuentra el personaje
                            Toast.makeText(MainActivity.this, "No se encontró el personaje", Toast.LENGTH_SHORT).show();
                            // Vaciar la lista
                            adapter.updateList(new ArrayList<>());
                            listView.setVisibility(View.GONE);
                            textViewNoResults.setVisibility(View.VISIBLE);
                        } else {
                            // Actualizar la lista con los resultados filtrados
                            adapter.updateList(filteredList);
                            listView.setVisibility(View.VISIBLE);
                            textViewNoResults.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        // Restaurar la lista original al borrar el texto
                        adapter.updateList(characterList);
                        listView.setVisibility(View.VISIBLE);
                        textViewNoResults.setVisibility(View.GONE);
                        return true;
                    }

                    // Filtrar la lista en tiempo real
                    List<Character> filteredList = new ArrayList<>();
                    for (Character character : characterList) {
                        if (character.getName().toLowerCase().contains(newText.toLowerCase())) {
                            filteredList.add(character);
                        }
                    }

                    if (filteredList.isEmpty()) {
                        // No se encontraron personajes
                        listView.setVisibility(View.GONE);
                        textViewNoResults.setVisibility(View.VISIBLE);
                    } else {
                        // Se encontraron personajes
                        listView.setVisibility(View.VISIBLE);
                        textViewNoResults.setVisibility(View.GONE);
                    }

                    // Actualiza el adaptador con la lista filtrada
                    adapter.updateList(filteredList);
                    return true;
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "SearchView no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateToolbar(); // Actualizar la Toolbar cada vez que se reanuda la actividad

    }
    /**
     * Actualiza el botón de la cuenta en la Toolbar.
     */
    private void updateToolbar() {
        if (buttonAccount == null) {
            Log.e("MainActivity", "buttonAccount es null. Verifica que está inicializado correctamente.");
            return;
        }
        if (firebaseAuth.getCurrentUser() != null) {
            buttonAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_account_circle_24, 0, 0, 0);
            buttonAccount.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            });

        } else {
            buttonAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_login_white_24, 0, 0, 0);
            buttonAccount.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido para enviar notificaciones", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado para notificaciones", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkLastVisitAndNotify() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        long lastVisit = sharedPreferences.getLong("last_visit", 0);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastVisit;

        // Si han pasado más de 7 días (604800000 ms), envía una notificación
        if (timeDifference > 604800000) {
            notificationHelper.notifySpecialEvent();
        }

        // Actualiza la última visita
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_visit", currentTime);
        editor.apply();
    }

}
