plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

apply(plugin = "com.google.dagger.hilt.android")

android {
    namespace = "com.rokas.showuswhatyougot.storage"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
}

