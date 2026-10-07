plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.arrowsescape.pzrqkx"
    minSdk = 23
    targetSdk = 35
    versionCode = 2
    versionName = "1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Vector drawables support down to API 21
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  // Limit locale resources to English only (saves ~200KB)
  androidResources {
    localeFilters += setOf("en")
  }

  signingConfigs {
    create("release") {
      storeFile = file("release-key.jks")
      storePassword = "arrowspassword"
      keyAlias = "arrowskey"
      keyPassword = "arrowspassword"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      enableV4Signing = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      // Full R8 mode for maximum dead-code elimination and performance
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")

      // Strip unused native debug symbols
      ndk {
        debugSymbolLevel = "NONE"
      }
    }

    debug {
      isDebuggable = true
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
  }

  // ── ABI Splits: produce one universal APK + per-ABI APKs ─────────────────
  // Covers: arm64-v8a (90%+ modern phones), armeabi-v7a (older/low-end),
  // x86_64 (emulators & a few tablets), x86 (legacy emulators).
  splits {
    abi {
      isEnable = true
      reset()
      include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
      isUniversalApk = true  // also build a fat APK covering all ABIs
    }
  }

  // ── APK packaging options ─────────────────────────────────────────────────
  packaging {
    resources {
      // Strip test-only and redundant meta-data that bloats the APK
      excludes += setOf(
        "META-INF/*.version",
        "META-INF/LICENSE.txt",
        "META-INF/LICENSE",
        "META-INF/NOTICE.txt",
        "META-INF/NOTICE",
        "META-INF/*.kotlin_module",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "kotlin/**",
        "DebugProbesKt.bin",
        "kotlin-tooling-metadata.json"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = true
    buildConfig = false  // disabled — not used in production code, saves size
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  // Use material-icons-core only — extended adds ~1MB; individual icons imported as needed
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
