plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Kotlinx serialization plugin (version managed here)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply false
}