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

### Network / security
- `network_security_config.xml` permits cleartext (`http://`) traffic, since many free
  IPTV sources aren't served over TLS.
- The player's `DefaultHttpDataSource.Factory` allows cross-protocol redirects
  (http ⇄ https), since many stream CDNs redirect between the two.

### Custom per-channel HTTP headers
Some protected/anti-hotlink stream sources reject playback unless specific headers
(`Referer`, `Origin`, `User-Agent`, `Cookie`, an auth token, etc.) are sent with every
request. `Channel` supports this via a headers map that's applied right before that
channel starts playing:

```java
Map<String, String> headers = new HashMap<>();
headers.put("Referer", "https://example.com/");
headers.put("Origin", "https://example.com");
headers.put("User-Agent", "Mozilla/5.0 ...");

Channel channel = new Channel(
        "Channel Name", "Category", "https://example.com/live/index.m3u8",
        "https://example.com/logo.png", "Description", 108, headers);
```

The headers are stored as a JSON string on the `Channel` row (no extra Room
`TypeConverter` needed) and are pushed onto the `DefaultHttpDataSource.Factory` in
`PlayerActivity.playChannel()` right before `player.prepare()`, so every HTTP
request for that channel's manifest/segments carries them.
