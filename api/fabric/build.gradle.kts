plugins {
    id("multiloader-api-loader")
    id("dev.kikugie.loom-back-compat") version "0.4"
    // id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

stonecutter {

}

dependencies {
    minecraft("com.mojang:minecraft:${commonEnv.mc}")
    if (!project.loomx.isUnobfuscated) mappings(loom.layered {
        officialMojangMappings()
        commonEnv.depOrNull("parchment")?.let { parchmentVersion ->
            parchment("org.parchmentmc.data:parchment-${commonEnv.mc}:$parchmentVersion@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:${commonEnv.dep("fabric_loader")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${commonEnv.dep("fabric_api")}+${commonEnv.mc}")

    // DevAuth for authentication
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

    modApi("net.kyori:adventure-platform-fabric:${env.depLoader("adventure-platform")}")
}

loom {
    splitEnvironmentSourceSets()
}