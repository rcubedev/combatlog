import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

plugins {
    id("multiloader-loader")
    id("dev.kikugie.loom-back-compat") version "0.4"
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

    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric_api")}+${commonMod.mc}")

    //fixme this will break 26.1+ unless loom-back-compat readds it
    implementation(project(path = ":mod:fabric:${commonMod.mc}", configuration = "namedRuntimeElements"))

    // DevAuth for authentication
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

    modImplementation("net.kyori:adventure-platform-fabric:${mod.depLoader("adventure-platform")}")
}

loom {
    splitEnvironmentSourceSets()
}

val clientJar by tasks.registering(Jar::class) {
    archiveClassifier.set("client")
    destinationDirectory.set(layout.buildDirectory.dir("client-libs"))
    from(sourceSets["client"].output)
}
val namedRuntimeElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations.runtimeClasspath)
}
val namedClientRuntimeElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations["clientRuntimeClasspath"])
    outgoing.artifact(clientJar)
}
configurations.matching { it.name == "namedElements" }.configureEach {
    namedRuntimeElements.extendsFrom(this)
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
//    runs {
//        getByName("client") {
//            client()
//            configName = "Fabric Client"
//            runDir = "run/client"
//            source(sourceSets["client"])
//            ideConfigGenerated(true)
//        }
//        getByName("server") {
//            server()
//            configName = "Fabric Server"
//            runDir = "run/server"
//            source(sourceSets["server"])
//            ideConfigGenerated(true)
//        }
//    }
}

publishMods {
    val modJar = loomx.modJar

    val modrinthStaging = envTrue("PUB_MODRINTH_STAGING")
    val modrinthAccessToken = env("PUB_MODRINTH_TOKEN")
    if (envTrue("PUB_DRY_RUN") || !envTrue("PUB_MODS_ENABLE")) dryRun = true
    val loaderName = "Fabric" // todo dyn

    file = modJar.flatMap { it.archiveFile }
    type = ReleaseType.STABLE // todo dyn idk how
    version = commonMod.version
    changelog = modChangelog
    modLoaders.add(loader)
    displayName = "${commonMod.name} ${commonMod.version} for $loaderName ${commonMod.mc}"

    modrinth {
        if (modrinthStaging) apiEndpoint = "https://staging-api.modrinth.com/v2"

        accessToken = modrinthAccessToken

        projectId = "CmOWKJeL"
        environment = ModrinthEnvironment.DEDICATED_SERVER_ONLY // todo add dyn
        minecraftVersions.add(commonMod.mc)

        requires("combatlogx-port")
    }
}