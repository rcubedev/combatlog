plugins {
    id("multiloader-loader")
    id("dev.kikugie.loom-back-compat") version "0.2"
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

stonecutter {

}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (!project.loomx.isUnobfuscated) mappings(loom.layered {
        officialMojangMappings()
        commonMod.depOrNull("parchment")?.let { parchmentVersion ->
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:$parchmentVersion@zip")
        }
    })

    implementation("com.github.rcubedev:java_utils")

    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric_api")}+${commonMod.mc}")

    // Required dependencies
    modImplementation("com.terraformersmc:modmenu:${commonMod.dep("modmenu")}")
    modImplementation("me.lucko:fabric-permissions-api:${commonMod.dep("fabric-permissions-api")}")
    include("me.lucko:fabric-permissions-api:${commonMod.dep("fabric-permissions-api")}")
    // Optional dependencies
    compileOnly("net.luckperms:api:${commonMod.dep("luckperms-api")}")
    // DevAuth for authentication
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

    // todo
    modImplementation("net.kyori:adventure-platform-fabric:5.14.2")
    // modImplementation("net.kyori:adventure-platform-mod-shared:6.0.1")
    // modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:6.0.1")
    // modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:6.2.0")}

    modImplementation("eu.pb4:placeholder-api:2.4.2+1.21")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")

    implementation("folk.sisby:kaleido-config:0.3.3+1.3.2")
    include("folk.sisby:kaleido-config:0.3.3+1.3.2")
}

loom {
    splitEnvironmentSourceSets()
}

sourceSets {
    val server by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

loom {
    mods {
        create(commonMod.id) {
            sourceSet(sourceSets["client"])
            sourceSet(sourceSets["server"])
        }
    }
    runs {
        getByName("client") {
            client()
            configName = "Fabric Client"
            runDir = "run/client"
            source(sourceSets["client"])
            ideConfigGenerated(true)
        }
        getByName("server") {
            server()
            configName = "Fabric Server"
            runDir = "run/server"
            source(sourceSets["server"])
            ideConfigGenerated(true)
        }
    }
}