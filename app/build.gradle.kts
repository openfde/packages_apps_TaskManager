plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

android {
    namespace = "com.fde.taskmanager"
    compileSdk = 35


    signingConfigs {
        create("release") {
            storeFile = file("plugin.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        getByName("debug") {
            storeFile = file("plugin.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.fde.taskmanager.debug"
        minSdk = 34
        targetSdk = 36
        versionCode = 60
        versionName = "6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
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

gradle.projectsEvaluated {
    tasks.withType<JavaCompile>().configureEach {
        val extraJar = File("libs/openfde_sdk.jar")

        // 安全处理 bootstrapClasspath（可能为 null）
        val currentBootstrap = options.bootstrapClasspath?.files ?: emptySet()
        val newBootstrap = listOf(extraJar) + currentBootstrap

        options.bootstrapClasspath = files(newBootstrap)
    }
}

dependencies {
    val versionNav = "2.9.0"
    val versionSer = "1.6.0"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    compileOnly(files("libs/openfde_sdk.jar"))

}