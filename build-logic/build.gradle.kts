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
    implementation(plugin("net.fabricmc.fabric-loom-remap", "1.17-SNAPSHOT"))
    implementation(plugin("me.modmuss50.mod-publish-plugin", "2.2.0"))
    implementation("dev.kikugie:stonecutter:0.7")
}