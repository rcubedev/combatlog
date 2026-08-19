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

    api("com.github.rcubedev:java_utils")
    include("com.github.rcubedev:java_utils")

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

    modImplementation("net.kyori:adventure-platform-fabric:${mod.depLoader("adventure-platform")}")
    include("net.kyori:adventure-platform-fabric:${mod.depLoader("adventure-platform")}")
    // modImplementation("net.kyori:adventure-platform-mod-shared:6.0.1")
    // modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:6.0.1")
    // modImplementation("net.kyori:adventure-platform-mod-shared-fabric-repack:6.2.0")}

    modImplementation("eu.pb4:placeholder-api:${commonMod.dep("placeholder-api")}")
    include("eu.pb4:placeholder-api:${commonMod.dep("placeholder-api")}")

    implementation("net.kyori:adventure-text-minimessage:${commonMod.dep("adventure-api")}")
    include("net.kyori:adventure-text-minimessage:${commonMod.dep("adventure-api")}")

    implementation("net.kyori:adventure-text-serializer-plain:${commonMod.dep("adventure-api")}")
    include("net.kyori:adventure-text-serializer-plain:${commonMod.dep("adventure-api")}")

    implementation("net.kyori:adventure-text-serializer-legacy:${commonMod.dep("adventure-api")}")
    include("net.kyori:adventure-text-serializer-plain:${commonMod.dep("adventure-api")}")

    api("folk.sisby:kaleido-config:0.3.3+1.3.2")
    include("folk.sisby:kaleido-config:0.3.3+1.3.2")
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

        projectId = "YSRERqEd"
        environment = ModrinthEnvironment.DEDICATED_SERVER_ONLY // todo add dyn
        minecraftVersions.add(commonMod.mc)

        requires("fabric-api")
        embeds("fabric-permissions-api", "placeholder-api", "adventure-platform-mod")
        optional("luckperms")
    }
}