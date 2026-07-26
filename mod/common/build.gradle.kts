plugins {
    id("multiloader-common")
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
    minecraft(group = "com.mojang", name = "minecraft", version = commonMod.mc)
    if (!project.loomx.isUnobfuscated) mappings(loom.layered {
        officialMojangMappings()
        commonMod.depOrNull("parchment")?.let { parchmentVersion ->
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:$parchmentVersion@zip")
        }
    })

    compileOnly("net.luckperms:api:${commonMod.dep("luckperms-api")}")
    compileOnly("org.spongepowered:mixin:0.8.5")
    modCompileOnly("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")

    api("com.github.rcubedev:java_utils")

    // todo
    modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:${commonMod.dep("adventure-platform")}")
    implementation("net.kyori:adventure-text-minimessage:${commonMod.dep("adventure-api")}")
    implementation("net.kyori:adventure-text-serializer-plain:${commonMod.dep("adventure-api")}")
    implementation("net.kyori:adventure-text-serializer-legacy:${commonMod.dep("adventure-api")}")

    api("folk.sisby:kaleido-config:0.3.3+1.3.2")
}

/*tasks.jar {
    description = "Assembles a jar archive containing the main and client classes."
    from(sourceSets["client"].output)
}*/