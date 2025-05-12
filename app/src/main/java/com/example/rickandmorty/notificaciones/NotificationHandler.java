package com.example.rickandmorty.notificaciones;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.rickandmorty.MainActivity;
import com.example.rickandmorty.R;

public class NotificationHandler extends ContextWrapper {

    private NotificationManager manager;
    public static final String CHANNEL_HIGH_ID = "1";
    public static final String CHANNEL_LOW_ID = "2";
    private final String CHANNEL_HIGH_NAME = "HIGH PRIORITY";
    private final String CHANNEL_LOW_NAME = "LOW PRIORITY";

    private final String GROUP_NAME = "RICK_AND_MORTY_GROUP";
    public static final int GROUP_ID = 101;

    public NotificationHandler(Context base) {
        super(base);
        createChannels();
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void createChannels() {
        NotificationChannel highChannel = new NotificationChannel(CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);
        highChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationChannel lowChannel = new NotificationChannel(CHANNEL_LOW_ID, CHANNEL_LOW_NAME, NotificationManager.IMPORTANCE_LOW);
        lowChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(highChannel);
        getManager().createNotificationChannel(lowChannel);
    }

    public Notification.Builder createNotification(String title, String msg, boolean priority) {
        if (priority) {
            return createNotificationChannels(title, msg, CHANNEL_HIGH_ID);
        }
        return createNotificationChannels(title, msg, CHANNEL_LOW_ID);
    }

    private Notification.Builder createNotificationChannels(String title, String msg, String channel) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("msg", msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_launcher_foreground), "VIEW", pendingIntent).build();

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_name);

        return new Notification.Builder(getApplicationContext(), channel)
                .setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setGroup(GROUP_NAME)
                .setContentIntent(pendingIntent)
                .addAction(action)
                .setLargeIcon(image)
                .setStyle(new Notification.BigPictureStyle().bigPicture(image).bigLargeIcon((Bitmap) null));
    }

    public void publishGroup(boolean priority) {
        String channel = priority ? CHANNEL_HIGH_ID : CHANNEL_LOW_ID;

        Notification groupNotification = new Notification.Builder(this, channel)
                .setGroup(GROUP_NAME)
                .setGroupSummary(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        getManager().notify(GROUP_ID, groupNotification);
    }

    public void createAndPublishNotification(String title, String msg, boolean priority) {
        Notification.Builder builder = createNotification(title, msg, priority);
        // Envía la notificación con un ID único
        getManager().notify((int) System.currentTimeMillis(), builder.build());
    }

    public void notifyNewCharacters(int count) {
        // Crear un mensaje dinámico según la cantidad de personajes nuevos
        String title = "¡Nuevos personajes disponibles!";
        String message = "Explora los " + count + " personajes más recientes de Rick and Morty.";

        // Crear un Intent para abrir MainActivity al hacer clic en la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Configurar la notificación
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_HIGH_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // Cierra la notificación al hacer clic
                .setStyle(new Notification.BigTextStyle().bigText(message)); // Estilo expandible

        // Publicar la notificación
        getManager().notify((int) System.currentTimeMillis(), builder.build());
    }

    public void notifyCharacterFavorite(String characterName, String detail, int id) {
        // Crear un intent que abra la actividad con detalles del personaje
        Intent intent = new Intent(this, MainActivity.class); // Aquí puedes redirigir a una actividad específica
        intent.putExtra("character_name", characterName);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Crear la notificación
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_HIGH_ID)
                .setContentTitle("¡Curiosidades sobre " + characterName + "!")
                .setContentText(detail)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia el ícono según tu diseño
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Publicar la notificación
        getManager().notify(id, notification);
    }

    public void notifySpecialEvent(String title, String message, int id) {
        Log.d("NotificationTest", "Creando notificación con ID: " + id);

        // Intent y PendingIntent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Crear la notificación
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_HIGH_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        // Publicar la notificación
        getManager().notify(id, notification);
    }
}
