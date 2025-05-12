# RickAndMortyApp 

This Android application, built with Kotlin, consumes the [Rick and Morty API](https://rickandmortyapi.com/) to display character information in a user-friendly interface.

## Features

- Browse characters from the Rick and Morty universe
- View detailed information about each character
- Infinite scroll or pagination support
- Filter characters by name, status, or species
- Responsive design using Jetpack components

## Technologies Used

- Java & Android Studio
- Retrofit (for network requests)
- Coroutines (for asynchronous operations)
- ViewModel + LiveData (architecture components)
- Glide or Coil (for image loading)
- Jetpack Navigation Component
- Material Design

## Project Structure

```
RickAndMortyApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/     # Java source code
│   │   │   ├── res/      # UI resources
│   │   │   └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/javisiierra/RickAndMortyApp.git
   ```
2. Open the project in Android Studio.
3. Make sure you have the Android SDK installed.
4. Click **Run** or use a connected device/emulator.

## Notes

- Requires internet connection to fetch API data.
- `local.properties` is not included (SDK path configuration).
- Ensure your project has correct permissions for network access.

## Author

**Javier Sierra**  
GitHub: [@javisiierra](https://github.com/javisiierra)

---

Enjoy exploring the multiverse with Rick and Morty!
