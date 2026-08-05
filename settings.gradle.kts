import java.nio.file.Files
import java.util.Properties

// val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = true //!isCi
gradle.startParameter.isBuildCacheEnabled = true //!isCi
//gradle.startParameter.isConfigureOnDemand = true //!isCi using configuration caching now

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7"
    id("dev.kikugie.loom-back-compat") version "0.4"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "CombatLog"

// discover & include all subprojects
val stonecutterGroups = listOf("test", "mod") + discoverStonecutterGroups("expansions")

data class StonecutterMeta(val value: Map<ProjectDescriptor, Map<String, List<String>>>)
fun stonecutterMeta(groups: List<String>): StonecutterMeta {
    val map = mutableMapOf<ProjectDescriptor, Map<String, List<String>>>()
    for (group in groups) {
        include(group)
        val projectDesc = project(":$group")
        val propsPath = projectDesc.projectDir.toPath().resolve("gradle.properties")
        if (!Files.isRegularFile(propsPath)) continue

        val props = Properties().apply { Files.newInputStream(propsPath).use { load(it) } }
        val commonVersions = props.getProperty("stonecutter_enabled_common_versions")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val fabricVersions = props.getProperty("stonecutter_enabled_fabric_versions")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val forgeVersions = props.getProperty("stonecutter_enabled_forge_versions")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val neoforgeVersions = props.getProperty("stonecutter_enabled_neoforge_versions")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val dists = mapOf(
            "common" to commonVersions,
            "forge" to forgeVersions,
            "fabric" to fabricVersions,
            "neoforge" to neoforgeVersions
        ).filterValues { it.isNotEmpty() }
        map[projectDesc] = dists
        //val uniqueVersions = dists.values.flatten().distinct()
    }
    //println("StonecutterMeta ${map.toMap()}")
    return StonecutterMeta(map.toMap())
}

fun discoverStonecutterGroups(baseDirName: String): List<String> {
    val baseDirPath = layout.rootDirectory.dir(baseDirName).asFile.toPath()
    if (!Files.isDirectory(baseDirPath)) return emptyList()

    return Files.list(baseDirPath).use { stream ->
        // jank exclusion for now
        stream.filter { path -> path.fileName?.toString() != "next" && Files.isDirectory(path) &&
                Files.exists(path.resolve("gradle.properties")) }
            .map { path -> "$baseDirName:${path.fileName}" }
            .toList()
    }
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    val meta = stonecutterMeta(stonecutterGroups)
    meta.value.forEach { (descriptor, dists) ->
        val uniqueVersions = dists.values.flatten().distinct()

        create(descriptor) {
            dists.forEach { (branchName, branchVersions) ->
                versions(*uniqueVersions.toTypedArray())

                branch(branchName) {
                    versions(*branchVersions.toTypedArray())
                }
            }
        }
    }
//    create(rootProject) {
//        versions(*uniqueVersions.toTypedArray())
//
//        dists.forEach { (branchName, branchVersions) ->
//            branch(branchName) {
//                versions(*branchVersions.toTypedArray())
//            }
//        }
//    }
}

includeBuild("build-logic")
includeBuild("libs/java-utils") {
    name = "java_utils"
    dependencySubstitution {
        substitute(module("com.github.rcubedev:java_utils"))
            .using(project(":"))
    }
}