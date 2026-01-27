import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "fr.eurecom.flowie"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.eurecom.flowie"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // MapTiler (from local.properties)
        val maptilerApiKey: String =
            project.findProperty("MAPTILER_API_KEY") as String? ?: "MISSING_KEY"
        buildConfigField("String", "MAPTILER_API_KEY", "\"$maptilerApiKey\"")

        // Supabase (from local.properties)
        val supabaseUrl: String =
            rootProject.findProperty("SUPABASE_URL") as String? ?: "MISSING_SUPABASE_URL"
        val supabaseAnonKey: String =
            rootProject.findProperty("SUPABASE_ANON_KEY") as String? ?: "MISSING_SUPABASE_ANON_KEY"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // For local data storage (e.g. saved spots ids)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)

    implementation("androidx.compose.material:material-icons-extended")

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Maps
    implementation("org.maplibre.gl:android-sdk:10.2.0")

    // Ktor (Supabase uses Ktor)
    implementation(platform("io.ktor:ktor-bom:2.3.11"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-logging")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Supabase (ONE BOM, do NOT use auth-kt)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.4"))
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")

    // Location services
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
