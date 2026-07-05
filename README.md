# Elendheim Chords

A personal chord notebook for Android. Tap notes on a piano keyboard to build a chord, hear it instantly, and save the voicings you like under your own names — all in plain note-name format like `A4+C5+E5`.

## What it does

- **Build**: a scrollable piano keyboard from C2 to C7. Tap keys to add notes to your chord; tap again to take them out. Every added note sounds as you tap it.
- **Play**: hear the full chord with a warm, built-in synth. No samples, no downloads.
- **Save**: name a voicing (for example "Chorus pad") and it lands in your library along with its note formula.
- **Library**: play any saved chord with one tap, reopen it on the keyboard to tweak it, or delete it.

Everything is stored on the device. No accounts, no network, no permissions.

## Design

Dark mode first, built around a soft dusty red. Big touch targets, haptic key presses, and labels on every key so it never feels like work.

## Building

Open the project in Android Studio and run it, or from the command line:

```
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`. Requires JDK 17+ and the Android SDK (API 35). Minimum supported device is Android 8.0 (API 26).

Every push to `testing` or `main` also builds the APK on GitHub Actions; grab it from the workflow run's artifacts.

## Tech

- Kotlin and Jetpack Compose (Material 3)
- Additive synthesis through AudioTrack for chord playback
- SharedPreferences-backed JSON storage for the chord library
