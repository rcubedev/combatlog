import com.github.rcubedev.gradle.transform.FmlPacker

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.141"
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22"
}

neoForge {
    enable {
        version = commonMod.dep("neoforge")
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
    api("com.github.rcubedev:java_utils")
    jarJar("com.github.rcubedev:java_utils")
    compileOnly("net.luckperms:api:${commonMod.dep("luckperms-api")}")

    implementation("net.kyori:adventure-platform-neoforge:${commonMod.dep("adventure-platform")}")
    // fixme kinda jank
    injectModType("net.kyori:adventure-text-serializer-legacy:${commonMod.dep("adventure-api")}")

    api("folk.sisby:kaleido-config:0.3.3+1.3.2")

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
    runs {
        register("client") {
            client()
            gameDirectory = project.file("run/client")
            ideName = "NeoForge Client (${project.path})"
        }
        register("server") {
            server()
            gameDirectory = project.file("run/server")
            ideName = "NeoForge Server (${project.path})"
            systemProperty("mixin.debug", "true")
            systemProperty("mixin.debug.export", "true")
        }

        configureEach {
        }
    }

    parchment {
        commonMod.depOrNull("parchment")?.let {
            mappingsVersion = it
            minecraftVersion = commonMod.mc
        }
    }

    mods {
        register(commonMod.id) {
            sourceSet(sourceSets.main.get())
            /*sourceSet(sourceSets["client"])
            sourceSet(sourceSets["server"])*/
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