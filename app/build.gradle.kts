import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val cieloProperties = Properties().apply {
    val propertiesFile = rootProject.file("cielo.local.properties")
    if (propertiesFile.isFile) propertiesFile.inputStream().use(::load)
}

fun String.asBuildConfigString(): String = "\"" +
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n") +
    "\""

android {
    namespace = "com.marlena.martins.sellcieapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.marlena.martins.sellcieapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            buildConfigField("Boolean", "CIELO_EMULATOR_ENABLED", "false")
            buildConfigField("String", "CIELO_CLIENT_ID", "\"\"")
            buildConfigField("String", "CIELO_ACCESS_TOKEN", "\"\"")
        }
        create("cieloEmulator") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            buildConfigField("Boolean", "CIELO_EMULATOR_ENABLED", "true")
            buildConfigField("String", "CIELO_CLIENT_ID", cieloProperties.getProperty("CIELO_CLIENT_ID", "").asBuildConfigString())
            buildConfigField("String", "CIELO_ACCESS_TOKEN", cieloProperties.getProperty("CIELO_ACCESS_TOKEN", "").asBuildConfigString())
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.g0dkar.qrcode)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
