plugins {
    id("com.android.application")
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "io.github.myungpyo.stickystate.sample"
        minSdk = 21
        targetSdk = 31
        compileSdk = 31
        versionCode = 10000
        versionName = "1.0.0"
    }

    sourceSets {
        val main by getting
        main.java.srcDirs("build/generated/ksp/debug/kotlin")
        main.java.srcDirs("build/generated/ksp/debug/kotlin")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core"))
    ksp(project(":processor"))

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}