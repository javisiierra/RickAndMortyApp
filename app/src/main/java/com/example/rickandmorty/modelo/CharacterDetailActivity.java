package com.example.rickandmorty.modelo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rickandmorty.R;
import com.squareup.picasso.Picasso;

import java.lang.Character;
import java.util.ArrayList;
import java.util.List;

public class CharacterDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);

        ImageView imageViewDetail = findViewById(R.id.imageViewDetail);
        TextView textViewNameDetail = findViewById(R.id.textViewNameDetail);
        TextView textViewSpeciesDetail = findViewById(R.id.textViewSpeciesDetail);
        TextView textViewStatusDetail = findViewById(R.id.textViewStatusDetail);
        TextView textViewGenderDetail = findViewById(R.id.textViewGenderDetail);
        TextView textViewOriginDetail = findViewById(R.id.textViewOriginDetail);
        TextView textViewEpisodes = findViewById(R.id.textViewEpisodesDetail);
        Button buttonShare = findViewById(R.id.buttonShare);

        // Obtener datos desde el intent
        Intent intent = getIntent();
        String image = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        String species = intent.getStringExtra("species");
        String status = intent.getStringExtra("status");
        String gender = intent.getStringExtra("gender");
        String origin = intent.getStringExtra("origin");
        List<String> episodeUrls = intent.getStringArrayListExtra("episodes");


        // Establecer datos en la UI
        Picasso.get().load(image).into(imageViewDetail);
        textViewNameDetail.setText(name);
        textViewSpeciesDetail.setText(species);
        textViewStatusDetail.setText(status);
        textViewGenderDetail.setText(gender);
        textViewOriginDetail.setText(origin);
        if (episodeUrls != null && !episodeUrls.isEmpty()) {
            // Extraer los números de los episodios desde las URLs
            List<String> episodeNumbers = new ArrayList<>();
            for (String url : episodeUrls) {
                // Extraer el número del episodio de la URL
                String episodeNumber = url.substring(url.lastIndexOf("/") + 1);
                episodeNumbers.add("Episode " + episodeNumber);
            }

            // Unir los números de episodios en un texto legible
            String episodesText = TextUtils.join(", ", episodeNumbers);
            textViewEpisodes.setText(episodesText);
        } else {
            textViewEpisodes.setText("No episodes found.");
        }

        // Configurar el botón de compartir
        buttonShare.setOnClickListener(v -> {
            shareCharacterDetails(name, species, status, gender, origin, episodeUrls);
        });
    }

    // Metodo para compartir los detalles del personaje
    private void shareCharacterDetails(String name, String species, String status, String gender, String origin, List<String> episodeUrls) {
        // Crear el texto que se compartirá
        StringBuilder shareText = new StringBuilder();
        shareText.append("Character: ").append(name).append("\n");
        shareText.append("Species: ").append(species).append("\n");
        shareText.append("Status: ").append(status).append("\n");
        shareText.append("Gender: ").append(gender).append("\n");
        shareText.append("Origin: ").append(origin).append("\n");

        if (episodeUrls != null && !episodeUrls.isEmpty()) {
            shareText.append("Episodes: ").append("\n");
            for (String url : episodeUrls) {
                String episodeNumber = url.substring(url.lastIndexOf("/") + 1);
                shareText.append("Episode " + episodeNumber).append("\n");
            }
        } else {
            shareText.append("No episodes found.").append("\n");
        }

        // Crear el Intent para compartir
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rick and Morty Character Details");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        // Mostrar el diálogo de compartir
        startActivity(Intent.createChooser(shareIntent, "Share character details"));
    }
}
