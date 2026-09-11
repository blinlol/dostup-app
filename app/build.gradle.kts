plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun envOrProp(name: String): String? =
    (findProperty(name) as String?) ?: System.getenv(name)

val appVersionName = (findProperty("appVersionName") as String?) ?: "0.0.0-dev"
val appVersionCode = (findProperty("appVersionCode") as String?)?.toInt() ?: 1

val releaseStorePassword = envOrProp("KEYSTORE_PASSWORD")
val releaseKeyAlias = envOrProp("KEY_ALIAS")
val releaseKeyPassword = envOrProp("KEY_PASSWORD")
val releaseStore = envOrProp("RELEASE_STORE_FILE")?.let { file(it) }?.takeIf { it.isFile }
val canSignRelease = releaseStore != null &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

android {
    namespace = "ru.wlwidget"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.wlwidget"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (canSignRelease) {
            create("release") {
                storeFile = checkNotNull(releaseStore)
                storePassword = checkNotNull(releaseStorePassword)
                keyAlias = checkNotNull(releaseKeyAlias)
                keyPassword = checkNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (canSignRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("org.json:json:20240303")
}
