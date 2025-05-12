package com.example.rickandmorty.api;

import com.example.rickandmorty.MainActivity;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadCharactersTask implements Runnable {
    private static final String API_URL = "https://rickandmortyapi.com/api/character";
    private MainActivity mainActivity;

    public DownloadCharactersTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                Gson gson = new Gson();
                Character[] characters = gson.fromJson(
                        new JsonParser().parse(json).getAsJsonObject().get("results"),
                        Character[].class
                );

                // Si la descarga es exitosa, mostrar notificación
                mainActivity.getNotificationHandler().createNotification(
                        "Nuevos personajes disponibles",
                        "Explora los nuevos personajes de Rick and Morty en la aplicación.",
                        true
                ).build();

                // Notifica a MainActivity
                mainActivity.runOnUiThread(() ->
                        mainActivity.updateUI(Arrays.asList(com.example.rickandmorty.modelo.Character[].class.cast(characters)))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
