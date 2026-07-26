plugins {
    id("java")
    id("idea")
    id("java-library")
}

//println("=== Applying multiloader-common plugin for ${loader}'s ${project.name}, isCommon=${isCommonLoader} ===")
//println("Via tree: :${commonProject.name}")
//println("Common loader: :${commonLoader}")
val gradleCommon = moduleProject.project(requireNotNull(commonLoader)).project(stonecutterBuild.current.project)
//println("Gradle common: $gradleCommon")
version = "${loader}-${commonMod.version}+mc${stonecutterBuild.current.version}"

base {
    archivesName = commonMod.id
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(commonProject.prop("java.version")!!)
    // withSourcesJar()
    // withJavadocJar()
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://repo.spongepowered.org/repository/maven-public") { name = "Sponge" }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            maven("https://maven.parchmentmc.org") { name = "ParchmentMC" },
            maven("https://maven.neoforged.net/releases") { name = "NeoForge" },
            maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }
    maven("https://www.cursemaven.com")
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://repo.sleeping.town")  // kaleido-config
    maven("https://maven.nucleoid.xyz") { name = "Nucleoid" } // text placeholder api
}

tasks {

    withType<ProcessResources>().configureEach {
        val expandProps = mapOf(
            "javaVersion" to commonMod.propOrNull("java.version"),
            "modId" to commonMod.id,
            "modName" to commonMod.name,
            "modVersion" to commonMod.version,
            "modGroup" to commonMod.group,
            "modAuthor" to commonMod.author,
            "modDescription" to commonMod.description,
            "modLicense" to commonMod.license,
            "modGitHub" to commonMod.github,
            "minecraftVersion" to commonMod.propOrNull("minecraft_version"),
            "minMinecraftVersion" to commonMod.propOrNull("min_minecraft_version"),
            "fabricLoaderVersion" to commonMod.depOrNull("fabric_loader"),
            "fabricApiVersion" to commonMod.depOrNull("fabric_api"),
            "neoForgeVersion" to commonMod.depOrNull("neoforge"),
            "forgeVersion" to commonMod.depOrNull("forge"),
            "toml4jVersion" to commonMod.depOrNull("toml4j"),
            "modMenuVersion" to commonMod.depOrNull("modmenu"),
            "fabricPermissionsApiVersion" to commonMod.depOrNull("fabric-permissions-api")
        ).filterValues { it?.isNotEmpty() == true }.mapValues { (_, v) -> v!! }

        val jsonExpandProps = expandProps.mapValues { (k, v) ->
            v.replace("\n", "\\\\n")
                .let {
                    if (k == "minecraftVersion" || k == "minMinecraftVersion") {
                        it.replace("-rc-", "-rc.").replace("-pre-", "-pre.")
                    } else it
                }
        }

        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
            expand(expandProps)
        }

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(jsonExpandProps)
        }

        inputs.properties(expandProps)
    }
}

/*tasks.register<Jar>("serverJar") {
    archiveAppendix.set("server")
    description = "Assembles a jar archive containing the main and server classes."
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets["main"].output)
    from(sourceSets["server"].output)
}

tasks.build {
    dependsOn("serverJar")
}*/

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    gradleCommon.tasks.findByName("stonecutterGenerate")?.let {
        dependsOn(it)
    }
//    dependsOn(":common:${commonMod.propOrNull("minecraft_version")}:stonecutterGenerate")
}

if (isCommonLoader) {
    afterEvaluate {
        // println()
        // sourceSets.all { sourceSet ->
        //     println("Common SourceSet '${sourceSet.name}':")
        //     println(" - Java dirs: ${sourceSet.java.srcDirs.joinToString()}")
        //     println(" - Resource dirs: ${sourceSet.resources.srcDirs.joinToString()}")
        //     true
        // }
        // println()
        val sourceSetConfigs = mutableMapOf<String, Pair<Configuration, Configuration>>()

        sourceSets.all { sourceSet ->
            val javaConfName = "${loader}${sourceSet.name.upperCaseFirst()}Java"
            val resourcesConfName = "${loader}${sourceSet.name.upperCaseFirst()}Resources"

            val javaConf = configurations.create(javaConfName) {
                isCanBeResolved = true
                isCanBeConsumed = true
            }
            val resourcesConf = configurations.create(resourcesConfName) {
                isCanBeResolved = true
                isCanBeConsumed = true
            }
            sourceSetConfigs[sourceSet.name] = javaConf to resourcesConf
//            println("Configuration: ${loader}${sourceSet.name.upperCaseFirst()}Java")
//            println("Configuration: ${loader}${sourceSet.name.upperCaseFirst()}Resources")
//            println("Created configuration for common source set: ${sourceSet.name} -> ${sourceSetConfigs.getValue(sourceSet.name).first.name}, ${sourceSetConfigs.getValue(sourceSet.name).second.name} ${sourceSetConfigs.getValue(sourceSet.name)}")
            true
        }

        artifacts {
            sourceSets.all { sourceSet ->
                val (javaConf, resourcesConf) = sourceSetConfigs.getValue(sourceSet.name)

                sourceSet.java.sourceDirectories.files.forEach {
                    add(javaConf.name, it)
                }
                sourceSet.resources.sourceDirectories.files.forEach {
                    add(resourcesConf.name, it)
                }
                true
            }
        }
    }

    // val ${loader}${commonSourceSet}Java: Configuration by configurations.creating {
    //     isCanBeResolved = false
    //     isCanBeConsumed = true
    // }

    // val ${loader}${commonSourceSet}Resources: Configuration by configurations.creating {
    //     isCanBeResolved = false
    //     isCanBeConsumed = true
    // }

    // artifacts {
    //     afterEvaluate {
    //         val ${loader}-${commonSourceSet}SourceSet = sourceSets.getByName(commonSourceSet)
    //         ${loader}-${commonSourceSet}SourceSet.java.sourceDirectories.files.forEach {
    //             add(${loader}Java${commonSourceSet}.name, it)
    //         }
    //         ${loader}-${commonSourceSet}SourceSet.resources.sourceDirectories.files.forEach {
    //             add(${loader}Java${commonSourceSet}.name, it)
    //         }
    //     }
    // }

    // === Shared Key Generation ===
    // Keys are now generated in the :keygen subproject
}