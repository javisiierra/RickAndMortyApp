package com.example.rickandmorty.modelo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.example.rickandmorty.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterAdapter extends BaseAdapter {
    private Context context;
    private List<Character> characters;
    private boolean isFavoriteList; // Identificar si es la lista de favoritos
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CharacterAdapter(Context context, List<Character> characters, boolean isFavoriteList) {
        this.context = context;
        this.characters = characters;
        this.isFavoriteList = isFavoriteList;
    }

    @Override
    public int getCount() {
        return characters.size();
    }

    @Override
    public Object getItem(int position) {
        return characters.get(position);
    }

    @Override
    public long getItemId(int position) {
        Character character = (Character) getItem(position);
        return character != null ? character.getId().hashCode() : 0; // Convertir el id String a hash
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.character_item, parent, false);

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageViewCharacter);
            holder.textViewName = convertView.findViewById(R.id.textViewName);
            holder.textViewSpecies = convertView.findViewById(R.id.textViewSpecies);
            holder.textViewStatus = convertView.findViewById(R.id.textViewStatus);
            holder.buttonFavorite = convertView.findViewById(R.id.buttonFavorite);
            holder.buttonRemove = convertView.findViewById(R.id.buttonRemove);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Configurar datos y lógica
        Character character = characters.get(position);
        holder.textViewName.setText(character.getName());
        holder.textViewSpecies.setText(character.getSpecies());
        holder.textViewStatus.setText(character.getStatus());
        Picasso.get().load(character.getImage()).into(holder.imageView);

        if (isFavoriteList) {
            holder.buttonRemove.setVisibility(View.VISIBLE);
            holder.buttonFavorite.setVisibility(View.GONE);
            holder.buttonRemove.setOnClickListener(v -> {
                characters.remove(position);
                notifyDataSetChanged();
                removeFavoriteFromFirestore(character);
            });
        } else {
            holder.buttonRemove.setVisibility(View.GONE);
            holder.buttonFavorite.setVisibility(View.VISIBLE);
            holder.buttonFavorite.setImageResource(
                    character.isFavorite() ? R.drawable.baseline_favorite_black_20 : R.drawable.baseline_favorite_border_black_20
            );
            holder.buttonFavorite.setOnClickListener(v -> {
                boolean newFavoriteStatus = !character.isFavorite();
                character.setFavorite(newFavoriteStatus);
                holder.buttonFavorite.setImageResource(
                        newFavoriteStatus ? R.drawable.baseline_favorite_black_20 : R.drawable.baseline_favorite_border_black_20
                );
                if (newFavoriteStatus) {
                    addFavoriteToFirestore(character);
                } else {
                    removeFavoriteFromFirestore(character);
                }
            });
        }

        // Set onClickListener to open the detail activity
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CharacterDetailActivity.class);
            intent.putExtra("image", character.getImage());
            intent.putExtra("name", character.getName());
            intent.putExtra("species", character.getSpecies());
            intent.putExtra("status", character.getStatus());
            intent.putExtra("gender", character.getGender());
            intent.putExtra("origin", character.getOrigin().getName());
            intent.putStringArrayListExtra("episodes", new ArrayList<>(character.getEpisode()));
            context.startActivity(intent);
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textViewName, textViewSpecies, textViewStatus;
        ImageButton buttonFavorite;
        ImageButton buttonRemove;
    }

    // Metodo para actualizar la lista de personajes
    public void updateList(List<Character> newList) {
        characters.clear();
        characters.addAll(newList);
        notifyDataSetChanged();
    }

    // Metodo para agregar un personaje a favoritos en Firestore
    private void addFavoriteToFirestore(Character character) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Por favor, inicia sesión para guardar favoritos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("id", character.getId());
        favoriteData.put("name", character.getName());
        favoriteData.put("image", character.getImage());

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(String.valueOf(character.getId()))
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Personaje añadido a favoritos", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "añadir");
                });
    }

    // Método para eliminar un personaje de favoritos en Firestore
    private void removeFavoriteFromFirestore(Character character) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Por favor, inicia sesión para eliminar favoritos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(String.valueOf(character.getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Personaje eliminado de favoritos", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(e, "eliminar");
                });
    }

    // Manejar errores de Firestore
    private void handleFirestoreError(Exception e, String action) {
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Toast.makeText(context, "No tienes permisos para " + action + " favoritos.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Error al " + action + " favorito: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
