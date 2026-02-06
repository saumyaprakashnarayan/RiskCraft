import java.security.MessageDigest
import java.security.KeyStore

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.riskcraft"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.riskcraft"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.register("generateSha1File") {
    doLast {
        val keystoreFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
        val resultFile = file("sha1_result.txt")
        if (keystoreFile.exists()) {
            try {
                val keystore = KeyStore.getInstance("JKS")
                keystore.load(keystoreFile.inputStream(), "android".toCharArray())
                val cert = keystore.getCertificate("androiddebugkey")
                val sha1 = MessageDigest.getInstance("SHA-1")
                    .digest(cert.encoded)
                    .joinToString(":") { "%02X".format(it) }
                resultFile.writeText(sha1)
            } catch (e: Exception) {
                resultFile.writeText("Error: ${e.message}")
            }
        } else {
            resultFile.writeText("Keystore not found at ${keystoreFile.absolutePath}")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
