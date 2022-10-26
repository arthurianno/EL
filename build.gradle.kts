buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.google.com")
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://oss.sonatype.org/content/repositories/releases")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Dependencies.gradleVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Dependencies.kotlinVersion}")
        classpath("com.google.gms:google-services:${Dependencies.Google.Services.servicesVersion}")
        classpath("com.google.firebase:firebase-plugins:${Dependencies.Google.FireBase.pluginVersion}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:${Dependencies.Google.FireBase.crashliticsGradleVersion}")
        classpath("io.objectbox:objectbox-gradle-plugin:${Dependencies.ObjectBox.version}")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:${Dependencies.ktLintVersion}")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Dependencies.detektGradlePluginVersion}")
//        classpath("com.github.ben-manes:gradle-versions-plugin:${Dependencies.dependenciesUpdateVersion}")
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.21.0-RC1"
    id("org.jlleitschuh.gradle.ktlint") version Dependencies.ktLintVersion
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    config = files("${project.projectDir}/detekt-config/config.yml")
    baseline = file("${project.projectDir}/detekt-config/baseline.xml")
}
