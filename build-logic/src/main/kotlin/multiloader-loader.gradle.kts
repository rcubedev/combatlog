import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.Copy

plugins {
    id("java")
    id("idea")
    id("multiloader-common")
}


//println("=== Applying multiloader-loader plugin for ${loader}'s ${project.name} ===")
//println("Loader source set dependencies:")
loaderSourceSetDeps.forEach { (sourceSetName, deps) ->
//    println(" - $sourceSetName depends on: ${deps.joinToString(", ")}")
}

afterEvaluate {
//    println("Attaching dependency source sets to loader source sets...")

    loaderSourceSetDeps.forEach { (loaderSSName, depKeys) ->
        val loaderSS = sourceSets[loaderSSName]
//        println("[multiloader-loader] Processing loader sourceset '$loaderSSName'")
//        println("[multiloader-loader]  Raw depKeys: $depKeys")

        // Deduplicate depKeys to avoid adding resources multiple times
        val uniqueDepKeys = depKeys.distinct()
//        println("[multiloader-loader]  Unique depKeys: $uniqueDepKeys")

        uniqueDepKeys.forEach { depKey ->
            val (depLoader, depSourceSetName) =
                depKey.split(':').let { if (it.size == 2) it else listOf("common", it[0]) }

            val javaConfName = "${depLoader}${depSourceSetName.upperCaseFirst()}Java"
            val resConfName = "${depLoader}${depSourceSetName.upperCaseFirst()}Resources"
            var stonecutterGenName: String = if (depSourceSetName != "main") { "stonecutterGenerate${depSourceSetName.upperCaseFirst()}" } else { "stonecutterGenerate" }

            val depProject = "${moduleProject.path}:$depLoader:${project.name}"

            val stonecutterGenTask = project(depProject).tasks.named(stonecutterGenName)
            //val stonecutterGenTask = project(":${depLoader}:${project.name}").tasks.named(stonecutterGenName)

            // Safely get or create the configurations
            val javaConf = configurations.maybeCreate(javaConfName)
            val resConf = configurations.maybeCreate(resConfName)

            dependencies {
                // add(
                //     loaderSS.implementationConfigurationName,
                //     project(path = ":$depLoader:${project.name}", configuration = javaConfName)
                // )
                javaConf(project(path = depProject, configuration = javaConfName))
                resConf(project(path = depProject, configuration = resConfName))
                //compileOnly(project(depProject))
                //fixme idk if this is right
                api(project(depProject))
            }

            // Attach dependency sources to compile/runtime tasks
            tasks {
                // val processResTask = tasks.named(loaderSS.processResourcesTaskName, Copy::class.java)
                // processResTask.configure {
                //     // dependsOn(":$depLoader:${project.name}:${resConfName}")
                //     from(project(":$depLoader:${project.name}").configurations.getByName(resConfName).files)
                // }
                // val compileTask = tasks.getByName(loaderSS.compileJavaTaskName) as JavaCompile
                // val processResTask = tasks.getByName(loaderSS.processResourcesTaskName) as Copy
                val compileTask = tasks.named(loaderSS.compileJavaTaskName, JavaCompile::class.java)
                val processResTask = tasks.named(loaderSS.processResourcesTaskName, Copy::class.java)

                compileTask.configure {
                    dependsOn(stonecutterGenTask)
                    dependsOn(javaConf)
                    source(javaConf)  // Add source files from dependency
                    loaderSS.compileClasspath += javaConf
                    loaderSS.runtimeClasspath += javaConf
                }

                processResTask.configure {
                    dependsOn(stonecutterGenTask)
                    dependsOn(resConf)
                    from(resConf)  // Include resource files from dependency
                    // loaderSS.resources.srcDirs(resConf)
                    
                    // FIXME :: Jank exclusion to prevent fabric.mod.json duplication in server jar, doesn't affect neoforge
                    if (loaderSSName != "main" && loaderSSName != "dev" && project.path.contains(":fabric")) {
                        exclude {
                            it.name == "fabric.mod.json"
                        }
                    }
                }
            }
        }
    }
}