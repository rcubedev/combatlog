import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.*

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()
fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

val Project.stonecutterBuild get() = extensions.getByType<StonecutterBuildExtension>()
val Project.stonecutterController get() = extensions.getByType<StonecutterControllerExtension>()

val Project.common get() = requireNotNull(stonecutterBuild.node.sibling("common")) {
    "No common project for $project"
}
val Project.commonProject get() = rootProject.project(stonecutterBuild.current.project)
val Project.commonMod get() = commonProject.mod

val Project.loader: String? get() = prop("loader")
val Project.commonLoader: String? get() = prop("loader.common")
val Project.isCommonLoader: Boolean get() {
    val commonLoaders = commonLoader?.split(',')?.map { it.trim() } ?: emptyList()
    return loader?.let { it in commonLoaders } == true
}
val Project.loaderSourceSetDeps: Map<String, List<String>> get() {
    // Grab all properties starting with "loader.sourceset." that end with ".dependsOn"
    val initialDeps = properties
        .filterKeys { it.startsWith("loader.sourceset.") && it.endsWith(".dependsOn") }
        .mapKeys { (key, _) ->
            key.removePrefix("loader.sourceset.").removeSuffix(".dependsOn")
        }
        .mapValues { (_, value) ->
            (value as? String).orEmpty()
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

    val resolvedDeps = mutableMapOf<String, MutableList<String>>()

    fun resolve(depKey: String, visited: MutableSet<String> = mutableSetOf()): List<String> {
        if (depKey in visited) return emptyList()
        visited += depKey

        val deps = initialDeps[depKey].orEmpty()
        // println("  [build-extensions.kt DEBUG] Resolving dependencies for sourceSet '$depKey': $deps")

        return deps.flatMap { depKey ->
            val (loaderPart, depSourceSet) = depKey.split(':').map { it.trim() }.let {
                if (it.size == 2) it else listOf("common", it[0])
            }
            // println("  [build-extensions.kt DEBUG]  - Found dependency '$depKey' -> loader='$loaderPart', sourceSet='$depSourceSet'")
            val transitives = resolve(depSourceSet, visited)
            // if (transitives.isNotEmpty()) println("  [build-extensions.kt DEBUG]    - Transitives for '$depKey': $transitives")
            listOf(depKey) + transitives
        }
    }

    initialDeps.keys.forEach { ss ->
        val allDeps = resolve(ss).distinct()
        // println("  [build-extensions.kt DEBUG] Final resolved dependencies for '$ss': $allDeps")
        resolvedDeps[ss] = allDeps.toMutableList()
    }

    return resolvedDeps
}

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = modProp("id")
    val name: String get() = modProp("name")
    val version: String get() = modProp("version")
    val group: String get() = modProp("group")
    val author: String get() = modProp("author")
    val description: String get() = modProp("description")
    val license: String get() = modProp("license")
    val github: String get() = modProp("github")
    val mc: String get() = depOrNull("minecraft") ?: project.stonecutterBuild.current.version

    fun propOrNull(key: String) = project.prop(key)
    fun prop(key: String) = requireNotNull(propOrNull(key)) { "Missing '$key'" }
    fun modPropOrNull(key: String) = project.prop("mod.$key")
    fun modProp(key: String) = requireNotNull(modPropOrNull(key)) { "Missing 'mod.$key'" }
    fun depOrNull(key: String): String? = project.prop("deps.$key")?.takeIf { it.isNotEmpty() && it != "" }
    fun dep(key: String) = requireNotNull(depOrNull(key)) { "Missing 'deps.$key'" }
    fun depLoader(key: String): String {
        val commonProject = project.commonProject;
        val loader = project.loader ?: return requireNotNull(commonProject.prop("deps.$key")) { "Missing 'deps.$key'" }
        val specific = loader.let { commonProject.prop("deps.$it.$key") }
        return requireNotNull(specific ?: commonProject.prop("deps.$key")) { "Missing $specific or deps.$key" }
    }
    fun modrinth(name: String, version:String) = "maven.modrinth:$name:$version"
}