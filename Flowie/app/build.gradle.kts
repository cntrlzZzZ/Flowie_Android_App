import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Apply the new Compose Compiler plugin
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

        val maptilerApiKey: String =
            project.findProperty("MAPTILER_API_KEY") as String? ?: "MISSING_KEY"

        buildConfigField(
            "String",
            "MAPTILER_API_KEY",
            "\"$maptilerApiKey\""
        )

        val supabaseUrl = project.findProperty("SUPABASE_URL") as String? ?: "MISSING_URL"
        val supabaseAnonKey = project.findProperty("SUPABASE_ANON_KEY") as String? ?: "MISSING_KEY"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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


configurations.all {
    resolutionStrategy {
        // This forces Gradle to use the specified Ktor version for all modules,
        // resolving any transitive dependency conflicts.
        force("io.ktor:ktor-client-core:2.3.11")
        force("io.ktor:ktor-client-okhttp:2.3.11")
        force("io.ktor:ktor-client-content-negotiation:2.3.11")
        force("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
        force("io.ktor:ktor-client-logging:2.3.11")
    }
}


kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

// In app/build.gradle.kts

dependencies {
    // added nw
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Use a BOM compatible with Kotlin 2.0. The 2024.06.00 version is recommended.
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(platform("androidx.compose:compose-bom:<version>"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7") // Version from BOM
    implementation("androidx.compose.material:material-icons-extended") // Version managed by BOM

    // other dependencies (keep them as they are)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("org.maplibre.gl:android-sdk:10.2.0")

    // 1. Add the Ktor Bill of Materials (BOM)
    implementation(platform("io.ktor:ktor-bom:2.3.11"))

    // 2. Remove the versions from individual Ktor modules
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // 3. Add a logging client for better debugging
    implementation("io.ktor:ktor-client-logging")


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.3.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Testing dependencies (keep them as they are)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
