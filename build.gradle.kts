plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    id("com.jfrog.artifactory")
    id("com.diffplug.spotless")
}

val kotlinVersion: String = findProperty("kotlinVersion") as? String ?: "1.6.0"
val accompanistVersion: String = findProperty("accompanistVersion") as? String ?: "0.22.0-rc"
val lifecycleVersion: String = findProperty("lifecycleVersion") as? String ?: "2.4.0"

version = "0.0.2"
group = "com.keygenqt.routing"

spotless {
    kotlin {
        target("**/*.kt")
        licenseHeaderFile("$buildDir/../LICENSE")
    }
}

publishing {
    publications {
        register("aar", MavenPublication::class) {
            groupId = group.toString()
            artifactId = project.name
            artifact("$buildDir/outputs/aar/compose-routing-$version-debug.aar")
        }
    }
}

artifactory {
    setContextUrl("https://artifactory.keygenqt.com/artifactory")
    publish {
        repository {
            setRepoKey("open-source")
            setUsername(findProperty("arusername").toString())
            setPassword(findProperty("arpassword").toString())
        }
        defaults {
            publications("aar")
            setPublishArtifacts(true)
        }
    }
}

android {

    compileSdk = 30

    defaultConfig {
        minSdk = 23
        targetSdk = 31
        setProperty("archivesBaseName", "compose-routing-$version")
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinVersion")
    implementation("androidx.navigation:navigation-compose:2.4.0-rc01")
}