plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.kikugie.dev/snapshots")
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"
    implementation(plugin("net.fabricmc.fabric-loom-remap", "1.16-SNAPSHOT")) // todo idk but breaks w/o it
    implementation("dev.kikugie:stonecutter:0.7")
}