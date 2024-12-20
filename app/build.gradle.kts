plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.ccisattendancechecker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ccisattendancechecker"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.material.v190)
    implementation (libs.play.services.base)
    implementation (libs.play.services.auth.v2070)

    implementation ("com.itextpdf:itextpdf:5.5.13.3")
    implementation ("com.github.yuriy-budiyev:code-scanner:2.3.2")
    implementation ("androidx.print:print:1.0.0")
    implementation ("com.github.Foysalofficial:pdf-viewer-android:6.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation (libs.zxing.android.embedded)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.multidex)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.firestore)
}