import com.github.rcubedev.gradle.transform.FmlPacker

plugins {
    id("multiloader-api-loader")
    id("net.neoforged.moddev") version "2.0.141"
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22"
}

neoForge {
    enable {
        version = commonEnv.dep("neoforge")
    }
}

fun Project.injectModType(dependencyNotation: String) {
    injectModType(dependencyNotation, FmlPacker.InjectionType.MANIFEST_LIBRARY)
}

fun Project.injectModType(dependencyNotation: String, type: FmlPacker.InjectionType) {
    injectModType(dependencyNotation, type, "", "")
}

fun Project.injectModType(dependencyNotation: String, type: FmlPacker.InjectionType, overrideModId: String, overrideDisplayName: String) {
    val detached = configurations.detachedConfiguration(dependencies.create(dependencyNotation) {
        isTransitive = false
    })

    val patchedFile = when (type) {
        FmlPacker.InjectionType.MANIFEST_LIBRARY -> {
            FmlPacker.patchManifestLibrary(detached, project.layout)
        }
        FmlPacker.InjectionType.NEO_MOD_TOML -> {
            FmlPacker.patchNeoModToml(detached, project.layout, overrideModId, overrideDisplayName)
        }
    }

    dependencies.add("implementation", files(patchedFile))
}

dependencies {
    implementation("com.github.rcubedev:java_utils")
    jarJar("com.github.rcubedev:java_utils")

    implementation(project(":mod:neoforge:${commonEnv.mc}"))

    implementation("net.kyori:adventure-platform-neoforge:${commonEnv.dep("adventure-platform")}")
    // fixme kinda jank
    // injectModType("net.kyori:adventure-text-serializer-legacy:${commonEnv.dep("adventure-api")}")

    if (stonecutter.eval(project.name, "<=1.21.1")) {
        "additionalRuntimeClasspath"("org.jetbrains:annotations:24.1.0")
        "additionalRuntimeClasspath"("folk.sisby:kaleido-config:0.3.3+1.3.2")
        "additionalRuntimeClasspath"("com.github.rcubedev:java_utils")
    }
}

sourceSets {
    /*val client by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    val server by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }*/
}

neoForge {
    parchment {
        commonEnv.depOrNull("parchment")?.let {
            mappingsVersion = it
            minecraftVersion = commonEnv.mc
        }
    }
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

/*tasks.jar {
    description = "Assembles a jar archive containing the main and client classes."
    from(sourceSets["client"].output)
}*/