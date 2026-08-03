plugins {
    id("multiloader-api-loader")
    id("dev.kikugie.loom-back-compat") version "0.4"
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

stonecutter {

}

loom {
    splitEnvironmentSourceSets()
}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (!project.loomx.isUnobfuscated) mappings(loom.layered {
        officialMojangMappings()
        commonMod.depOrNull("parchment")?.let { parchmentVersion ->
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:$parchmentVersion@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")
//    modApi("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric_api")}+${commonMod.mc}")

    //fixme this will break 26.1+ unless loom-back-compat readds it
    implementation(project(path = ":mod:fabric:${commonMod.mc}", configuration = "namedRuntimeElements"))
    "clientImplementation"(project(path = ":mod:fabric:${commonMod.mc}", configuration = "namedClientRuntimeElements"))

    implementation(project(path = ":expansions:action-bar:fabric:${commonMod.mc}", configuration = "namedRuntimeElements"))
    "clientImplementation"(project(path = ":expansions:action-bar:fabric:${commonMod.mc}", configuration = "namedClientRuntimeElements"))

    implementation(project(path = ":expansions:boss-bar:fabric:${commonMod.mc}", configuration = "namedRuntimeElements"))
    "clientImplementation"(project(path = ":expansions:boss-bar:fabric:${commonMod.mc}", configuration = "namedClientRuntimeElements"))

    // DevAuth for authentication
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

//    modImplementation("net.kyori:adventure-platform-fabric:${mod.depLoader("adventure-platform")}")
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