package com.example.rickandmorty.notificaciones;

import android.content.Context;
import android.widget.Toast;

import com.example.rickandmorty.modelo.Character;

import java.util.List;

public class NotificationHelper {

    private final NotificationHandler notificationHandler;

    public NotificationHelper(Context context) {
        this.notificationHandler = new NotificationHandler(context);
    }



    /**
     * Notifica sobre el personaje favorito (ejemplo: el primer personaje de la lista).
     *
     * @param characterList Lista de personajes descargados.
     */
    public void notifyFavoriteCharacter(List<Character> characterList) {
        if (characterList != null && !characterList.isEmpty()) {
            Character favoriteCharacter = characterList.get(0);
            String name = favoriteCharacter.getName();
            String detail = "¿Sabías que " + name + " es de la especie " + favoriteCharacter.getSpecies() + "?";

            notificationHandler.notifyCharacterFavorite(name, detail, 1001);
        }
    }

    /**
     * Notifica un evento especial.
     */
    public void notifySpecialEvent() {
        String title = "¡Evento Especial!";
        String message = "Explora los personajes destacados de Rick and Morty.";
        notificationHandler.notifySpecialEvent(title, message, 1002);
    }

    /**
     * Notifica un error en la descarga.
     */
    public void notifyDownloadError() {
        notificationHandler.createAndPublishNotification(
                "Error en la descarga",
                "No se pudieron descargar los personajes. Inténtalo nuevamente.",
                false
        );
    }

    /**
     * Notifica la finalización de la descarga de personajes.
     */
    public void notifyDownloadComplete() {
        notificationHandler.createAndPublishNotification(
                "Descarga completa",
                "Se han descargado nuevos personajes. ¡Explóralos ahora!",
                true
        );
    }

    public static Runnable showError(Context context) {
        Toast.makeText(context, "Error al cargar personajes", Toast.LENGTH_SHORT).show();
        return null;
    }
}

