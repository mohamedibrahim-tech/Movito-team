import org.gradle.kotlin.dsl.androidTestImplementation
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    // dokka for generating the documentation
    id("org.jetbrains.dokka") version "1.9.20" apply false
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.movito.movito"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.movito.movito"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "5.0.4"

        testInstrumentationRunner = "com.movito.movito.HiltTestRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val apiKey = localProperties.getProperty("tmdb_api_key")
        buildConfigField("String", "TMDB_API_KEY", "\"$apiKey\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.hilt.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase
    implementation("com.google.firebase:firebase-auth")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-firestore")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Testing
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("io.mockk:mockk-android:1.13.3")

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    //ListenableFuture compatibility
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("com.google.guava:guava:31.0.1-android")
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("androidx.test:core:1.5.0")
}

// Documentation task (dokka)
tasks.register<org.jetbrains.dokka.gradle.DokkaTask>("dokkaMovito") {
    moduleName.set("Movito Movie App")
    outputDirectory.set(file("../docs"))

    dokkaSourceSets {
        configureEach {
            sourceRoots.from(file("src/main/kotlin"))

            // Exclude test packages and generated code
            perPackageOption {
                matchingRegex.set(".*\\.test\\..*")
                suppress.set(true)
            }
            perPackageOption {
                matchingRegex.set(".*\\.di\\..*")  // Exclude Dagger generated files
                suppress.set(true)
            }
            perPackageOption {
                matchingRegex.set(".*\\.BuildConfig")  // Exclude BuildConfig
                suppress.set(true)
            }

            // Skip generated/synthetic files
            skipDeprecated.set(true)
            skipEmptyPackages.set(true)

            //includes.from("README.md")
        }
    }
}