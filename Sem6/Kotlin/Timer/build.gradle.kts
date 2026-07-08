// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.navigation.safeargs) apply false

    // Если студия сама добавила compose, можете оставить, но для проекта он не нужен
    // alias(libs.plugins.kotlin.compose) apply false
}
