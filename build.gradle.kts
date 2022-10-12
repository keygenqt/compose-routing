plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.diffplug.spotless")
}

version = "0.0.3"
group = "com.keygenqt.routing"

spotless {
    kotlin {
        target("**/*.kt")
        licenseHeaderFile("$buildDir/../LICENSE")
    }
}

android {

    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33
        setProperty("archivesBaseName", "compose-routing-$version")
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = findProperty("composeCompilerVersion").toString()
    }

    buildFeatures {
        compose = true
    }
}

// https://google.github.io/accompanist/
val accompanistVersion = "0.26.5-rc"
// https://developer.android.com/jetpack/androidx/releases/lifecycle
val lifecycleVersion = "2.5.1"
// https://github.com/Kotlin/kotlinx.coroutines/releases
val coroutinesVersion = "1.6.4"
// https://developer.android.com/jetpack/compose/navigation
val navigationComposeVersion = "2.5.2"

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("androidx.navigation:navigation-compose:$navigationComposeVersion")
}