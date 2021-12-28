plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    id("com.jfrog.artifactory")
}

// dependencies versions
val composeVersion: String by project

// lib info
val libVersion: String by project
val libGroup: String by project

publishing {
    publications {
        register("aar", MavenPublication::class) {
            version = libVersion
            groupId = libGroup
            artifactId = project.name
            artifact("$buildDir/outputs/aar/compose-routing-$libVersion-release.aar")
        }
    }
}

artifactory {
    setContextUrl("https://artifactory.surfstudio.ru/artifactory")
    publish {
        repository {
            setRepoKey("libs-release-local")
            setUsername(System.getenv("surf_maven_username"))
            setPassword(System.getenv("surf_maven_password"))
        }
        defaults {
            publications("aar")
            setPublishArtifacts(true)
        }
    }
}

android {

    compileSdk = 31

    defaultConfig {
        minSdk = 23
        targetSdk = 31
        setProperty("archivesBaseName", "compose-routing-$libVersion")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("com.google.accompanist:accompanist-pager:0.20.2")
    implementation("androidx.navigation:navigation-compose:2.4.0-rc01")
}