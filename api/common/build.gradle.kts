plugins {
    id("multiloader-api")
    id("dev.kikugie.loom-back-compat")
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

stonecutter {

}

fletchingTable {
    j52j.register("main") {
        extension("json", "**/*.json5")
    }
}

sourceSets {
    /*create("server") {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("client") {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }*/
}

loom {
    mixin {
        useLegacyMixinAp = false
    }

    interfaceInjection {
        enableDependencyInterfaceInjection = false
    }
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = commonEnv.mc)
    if (!project.loomx.isUnobfuscated) mappings(loom.layered {
        officialMojangMappings()
        commonEnv.depOrNull("parchment")?.let { parchmentVersion ->
            parchment("org.parchmentmc.data:parchment-${commonEnv.mc}:$parchmentVersion@zip")
        }
    })

    compileOnly("org.spongepowered:mixin:0.8.5")
    modCompileOnly("net.fabricmc:fabric-loader:${commonEnv.dep("fabric_loader")}")

    modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:${commonEnv.dep("adventure-platform")}")
    implementation("net.kyori:adventure-text-minimessage:${commonEnv.dep("adventure-api")}")
}

/*tasks.jar {
    description = "Assembles a jar archive containing the main and client classes."
    from(sourceSets["client"].output)
}*/