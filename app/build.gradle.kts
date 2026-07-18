plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.ottking.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    buildConfig = true
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Pure Java dependencies only - no Kotlin, no Compose.
dependencies {
  implementation("androidx.core:core:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("androidx.recyclerview:recyclerview:1.3.2")

  // Room (persistence) - using annotationProcessor, not KSP, since this is a Java-only project.
  implementation(libs.androidx.room.runtime)
  annotationProcessor(libs.androidx.room.compiler)

  // Image loading
  implementation("com.github.bumptech.glide:glide:4.16.0")
  annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

  // Media3 ExoPlayer with HLS (.m3u8) and DASH (.mpd) support, plus the player UI.
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.exoplayer.hls)
  implementation(libs.androidx.media3.exoplayer.dash)
  implementation(libs.androidx.media3.ui)

  // Tests
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}
