import java.util.Properties

val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = !isCi
gradle.startParameter.isBuildCacheEnabled = !isCi
gradle.startParameter.isConfigureOnDemand = !isCi

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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "CombatLog"

// discover & include all subprojects
val stonecutterGroups = listOf("mod")

data class StonecutterMeta(val value: Map<ProjectDescriptor, Map<String, List<String>>>)
fun stonecutterMeta(groups: List<String>): StonecutterMeta {
    val map = mutableMapOf<ProjectDescriptor, Map<String, List<String>>>()
    for (group in groups) {
        include(group)
        val projectDesc = project(":$group")
        val propsFile = projectDesc.projectDir.resolve("gradle.properties")
        if (!propsFile.isFile) continue

        val props = Properties().apply { propsFile.inputStream().use { load(it) } }

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
    println("StonecutterMeta ${map.toMap()}")
    return StonecutterMeta(map.toMap())
}

//val commonVersions = providers.gradleProperty("stonecutter_enabled_common_versions").orNull?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
//val fabricVersions = providers.gradleProperty("stonecutter_enabled_fabric_versions").orNull?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
//val forgeVersions = providers.gradleProperty("stonecutter_enabled_forge_versions").orNull?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
//val neoforgeVersions = providers.gradleProperty("stonecutter_enabled_neoforge_versions").orNull?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
//val dists = mapOf(
//    "common" to commonVersions,
//    "forge" to forgeVersions,
//    "fabric" to fabricVersions,
//    "neoforge" to neoforgeVersions
//).filterValues { it.isNotEmpty() }
//val uniqueVersions = dists.values.flatten().distinct()

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

includeBuild("../java-utils") {
    name = "java_utils"
    dependencySubstitution {
        substitute(module("com.github.rcubedev:java_utils"))
            .using(project(":"))
    }
}