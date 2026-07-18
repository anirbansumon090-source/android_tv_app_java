<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/c3c9aa9e-382e-4e91-8516-71706cac90df

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)

This is a pure Java Android project (no Kotlin, no Jetpack Compose).

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. For a release build, remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
5. Run the app on an emulator or physical (Android TV) device

## Player

The live player is built on **AndroidX Media3 ExoPlayer** and supports:
- HLS (`.m3u8`)
- DASH (`.mpd`)
- Plain progressive streams (`.mp4`, etc.)

`MediaItem` is built with an explicit MIME type hint so playlists that don't end in
`.m3u8`/`.mpd` (common with IPTV links that carry query strings/tokens) still resolve to
the correct HLS/DASH media source instead of falling through to the progressive extractor.
