// Suite-Kern: gemeinsamer Unterbau aller On-Device-Apps der Fabrik.
// Keine Abhaengigkeiten, keine Berechtigungen, kein Netz.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.ithandwerkstuttgart.suitekern"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
