import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

android {
    namespace = "com.example.taskmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.taskmanager"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    var coreLibraryDesugaringEnabled = true
    buildFeatures {
        compose = true
        aidl = true
    }
}

//configurations {
//    all {
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
//        exclude(group = "org.jetbrains", module = "annotations")
//        exclude(group = "org.jspecify", module = "jspecify")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
//        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
//        exclude(group = "androidx.annotation", module = "annotation-jvm")
//        exclude(group = "androidx.collection", module = "collection-jvm")
//        exclude(group = "androidx.arch.core", module = "core-common")
//        exclude(group = "com.google.code.gson", module = "gson")
//        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
//        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
//        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
//        exclude(group = "androidx.lifecycle", module = "lifecycle-common-jvm")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions")
//        exclude(group = "androidx.core", module = "core")
//        exclude(group = "androidx.core", module = "core-ktx")
//        exclude(group = "androidx.appcompat", module = "appcompat")
//        exclude(group = "androidx.constraintlayout", module = "constraintlayout")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
//        exclude(group = "androidx.compose.ui", module = "ui-tooling")
//        exclude(group = "org.jetbrains.kotlin", module = "annotation-experimental")
//        exclude(group = "androidx.compose", module = "ui-tooling-release-api")
//        exclude(group = "com.android", module = "android")
//    }
//}

dependencies {
    val versionNav = "2.9.0"
    val versionSer = "1.6.0"
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:$versionNav")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$versionSer")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.google.code.gson:gson:2.13.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

}