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
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath("io.objectbox:objectbox-gradle-plugin:${Dependencies.ObjectBox.version}")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.21.0-RC1")
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.21.0-RC1"
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
