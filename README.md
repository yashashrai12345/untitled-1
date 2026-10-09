# Arrows — Puzzle Escape

An offline puzzle game built natively for Android using Kotlin and Jetpack Compose.

## Overview

**Arrows — Puzzle Escape** challenges players to guide entangled arrows off the game board without collisions. Each arrow moves along its directional vector, requiring players to deduce the correct escape sequence.

### Key Features
- **100 Playable Levels Offline**:
  - Levels 1–35: Handcrafted silhouette puzzle boards (Maze, Heart, Cup, Round, Leaf, Diamond, Star).
  - Levels 40–100: Procedurally generated, verified solvable levels with increasing complexity.
- **Pure Local Architecture**: Completely offline gameplay with no external server, database, or network dependency.
- **Modern Jetpack Compose UI**: Smooth animations, haptic feedback, and audio cues.
- **Progression & Statistics**: Saved player progress, 3-star rating system, and hint system.

---

## Getting Started

### Prerequisites
- [Android Studio Ladybug | 2024.2+](https://developer.android.com/studio)
- JDK 17+
- Android SDK 35 (Android 15)

### Running Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/yashashrai12345/untitled-1.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle sync to complete.
4. Run the app on an Android Emulator (API 34+) or physical Android device.

---

## Project Structure

```
├── app/
│   ├── src/main/java/com/example/
│   │   ├── audio/           # SoundManager & audio effects
│   │   ├── data/            # Local LevelRepository & GamePreferences
│   │   ├── game/            # Game engine, collision detection, solver, generator
│   │   ├── haptics/         # Haptic feedback system
│   │   └── ui/              # Jetpack Compose screens, components, and themes
│   └── src/test/java/       # JVM unit tests & Robolectric tests
├── gradle/                  # Gradle wrapper configuration
├── build.gradle.kts         # Root build configuration
└── settings.gradle.kts      # Project settings
```

---

## Testing & Verification

Run all unit tests:
```bash
./gradlew.bat testDebugUnitTest
```

Build debug APK:
```bash
./gradlew.bat assembleDebug
```
