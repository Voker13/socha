import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.InputStream

plugins {
    maven
    `java-library`
    kotlin("jvm") version "1.3.61"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("org.jetbrains.dokka") version "0.10.0"
}

val gameName by extra { property("socha.gameName") as String }
val versions = arrayOf("year", "minor", "patch").map { property("socha.version.$it").toString().toInt() }
val versionObject = KotlinVersion(versions[0], versions[1], versions[2])
version = versions.joinToString(".") { it.toString() }
val year by extra { "20${versionObject.major}" }
val game by extra { "${gameName}_$year" }
println("Current version: $version Game: $game")

val deployDir by extra { buildDir.resolve("deploy") }
val deployedPlayer: String by extra { deployDir.resolve("simpleclient-$gameName-$version.jar").absolutePath }
val testLogDir by extra { buildDir.resolve("tests") }

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
}

val doAfterEvaluate = ArrayList<(Project) -> Unit>()
val mainGroup = "_main"
tasks {
    val startServer by creating {
        dependsOn(":server:run")
        group = mainGroup
    }
    
    val doc by creating(DokkaTask::class) {
                outputDirectory = deployDir.resolve("doc").toString()
                outputFormat = "javadoc"
            subProjects = listOf("sdk", "plugin")
    }
    
    val deploy by creating {
        dependsOn(doc)
        dependOnSubprojects()
        group = mainGroup
        description = "Zips everything up for release into build/deploy/"
    }
    
    val release by creating {
        dependsOn(deploy)
        group = mainGroup
        description = "Prepares a new Release by bumping the version and creating a commit and a git tag"
        doLast {
            fun edit(original: String, version: String, new: Int) =
                    if(original.startsWith("socha.version.$version"))
                        "socha.version.$version=${new.toString().padStart(2, '0')}"
                    else original
            
            var newVersion = version
            val filter: (String) -> String = when {
                project.hasProperty("manual") -> ({ it })
                project.hasProperty("minor") -> ({
                    newVersion = "${versionObject.major}.${versionObject.minor + 1}.0"
                    edit(edit(it, "minor", versionObject.minor + 1), "patch", 0)
                })
                project.hasProperty("patch") -> ({
                    newVersion = "${versionObject.major}.${versionObject.minor}.${versionObject.patch + 1}"
                    edit(it, "patch", versionObject.patch + 1)
                })
                else -> throw InvalidUserDataException("Gib entweder -Ppatch oder -Pminor an, um die Versionsnummer automatisch zu inkrementieren, oder ändere sie selbst in gradle.properties und gib dann -Pmanual an!")
            }
            val desc = project.properties["m"]?.toString()
                    ?: throw InvalidUserDataException("Das Argument -Pm=\"Beschreibung dieser Version\" wird benötigt")
    
            val propsFile = file("gradle.properties")
            propsFile.writeText(
                    propsFile.readLines().joinToString("\n") { filter(it) }
            )
            
            println("Version: $newVersion")
            println("Beschreibung: $desc")
            exec { commandLine("git", "add", "gradle.properties") }
            exec { commandLine("git", "commit", "-m", "release: $newVersion", "--no-verify") }
            exec { commandLine("git", "tag", newVersion, "-m", desc) }
            exec { commandLine("git", "push", "--follow-tags") }
        }
    }
    
    val maxGameLength = 150L
    
    val clearTestLogs by creating(Delete::class) {
        delete(testLogDir)
    }
    
    val testGame by creating {
        dependsOn(clearTestLogs, ":server:deploy", ":player:deploy")
        doFirst {
            testLogDir.mkdirs()
            val server = ProcessBuilder("java", "-Dlogback.configurationFile=logback.xml", "-jar",
                    project("server").tasks.jar.get().archiveFile.get().asFile.absolutePath)
                    .redirectOutput(testLogDir.resolve("server.log")).redirectError(testLogDir.resolve("server-err.log"))
                    .directory(project("server").buildDir.resolve("runnable")).start()
            Thread.sleep(1000)
            val startClient: (Int) -> Process = {
                ProcessBuilder("java", "-jar", deployedPlayer)
                        .redirectOutput(testLogDir.resolve("client$it.log")).redirectError(testLogDir.resolve("client$it-err.log")).start()
            }
            startClient(1)
            startClient(2)
            val thread = Thread {
                try {
                    Thread.sleep(maxGameLength * 1000)
                } catch(e: InterruptedException) {
                    return@Thread
                }
                println("$this has been running for over $maxGameLength seconds - killing server!")
                server.destroyForcibly()
            }.apply {
                isDaemon = true
                start()
            }
            try {
                for(i in 1..2) {
                    println("Waiting for client $i to receive game result")
                    do {
                        if(!server.isAlive)
                            throw Exception("Server terminated unexpectedly!")
                        Thread.sleep(200)
                    } while(!testLogDir.resolve("client$i.log").readText().contains("Received game result", true))
                }
            } catch(t: Throwable) {
                println("Error in $this - check the logs in $testLogDir")
                throw t
            } finally {
                server.destroy()
            }
            thread.interrupt()
            println("Successfully played a game using the deployed server & client!")
        }
    }
    
    val testTestClient by creating {
        dependsOn(clearTestLogs, ":server:deploy")
        val testClientGames = 3
        doFirst {
            val tmpDir = buildDir.resolve("tmp")
            tmpDir.mkdirs()
            testLogDir.mkdirs()
            val unzipped = tmpDir.resolve("software-challenge-server")
            unzipped.deleteRecursively()
            Runtime.getRuntime().exec("unzip software-challenge-server.zip -d $unzipped", null, deployDir).waitFor()
            
            println("Testing TestClient...")
            val testClient = ProcessBuilder(
                    project("test-client").tasks.getByName<ScriptsTask>("createScripts").content.split(" ") +
                            listOf("--start-server", "--tests", "$testClientGames"))
                    .redirectOutput(testLogDir.resolve("test-client.log")).redirectError(testLogDir.resolve("test-client-err.log"))
                    .directory(unzipped).start()
            if(testClient.waitFor(maxGameLength * testClientGames, TimeUnit.SECONDS)) {
                val value = testClient.exitValue()
                if(value == 0)
                    println("TestClient successfully tested!")
                else
                    throw Exception("TestClient exited with exit code $value!")
            } else {
                throw Exception("TestClient exceeded timeout of ${maxGameLength * testClientGames} seconds!")
            }
        }
    }
    
    val integrationTest by creating {
        enabled = versionObject.minor > 0
        if(enabled)
            dependsOn(testGame, testTestClient)
        group = mainGroup
    }
    
    clean {
        dependOnSubprojects()
        group = mainGroup
    }
    test {
        dependOnSubprojects()
        dependsOn(integrationTest)
        group = mainGroup
    }
    build {
        group = mainGroup
    }
}

// == Cross-project configuration ==

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    
    repositories {
        jcenter()
        maven("http://dist.wso2.org/maven2")
    }
    if(this.name in arrayOf("sdk", "plugin")) {
        apply(plugin = "maven")
        tasks {
            val doc by creating(DokkaTask::class) {
                outputDirectory = buildDir.resolve("doc").toString()
                outputFormat = "javadoc"
                configuration {
                    moduleName = "Software-Challenge API $version"
                    jdkVersion = 8
                }
            }
            val docJar by creating(Jar::class) {
                dependsOn(doc)
                archiveBaseName.set(jar.get().archiveBaseName)
                archiveClassifier.set("javadoc")
                from(doc.outputDirectory)
            }
            val sourcesJar by creating(Jar::class) {
                archiveBaseName.set(jar.get().archiveBaseName)
                archiveClassifier.set("sources")
                from(sourceSets.main.get().allSource)
            }
            install.get().dependsOn(docJar, sourcesJar)
            artifacts {
                archives(sourcesJar.archiveFile) { classifier = "sources" }
                archives(docJar.archiveFile) { classifier = "javadoc" }
            }
        }
    }
    afterEvaluate {
        doAfterEvaluate.forEach { it(this) }
        tasks {
            forEach { if(it.name != clean.name) it.mustRunAfter(clean.get()) }
            test {
                testLogging { showStandardStreams = project.properties["verbose"] != null }
            }
            withType<Jar> {
                if(plugins.hasPlugin(ApplicationPlugin::class))
                    manifest.attributes["Main-Class"] = project.extensions.getByType<JavaApplication>().mainClassName
            }
        }
    }
}

project("sdk") {
    sourceSets.main.get().java.srcDirs("src/framework", "src/server-api")
    
    dependencies {
        api(kotlin("stdlib"))
        api("com.thoughtworks.xstream", "xstream", "1.4.11.1")
        api("jargs", "jargs", "1.0")
        api("ch.qos.logback", "logback-classic", "1.2.3")
        
        implementation("org.hamcrest", "hamcrest-core", "2.2")
        implementation("net.sf.kxml", "kxml2", "2.3.0")
        implementation("xmlpull", "xmlpull", "1.1.3.1")
    }
}

project("plugin") {
    sourceSets {
        main.get().java.srcDirs("src/client", "src/server", "src/shared")
        test.get().java.srcDir("src/test")
    }
    
    dependencies {
        api(project(":sdk"))
        
        testImplementation("junit", "junit", "4.12")
        testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.4.2")
    }
    
    tasks.jar.get().archiveBaseName.set(game)
}

// == Utilities ==

fun InputStream.dump(name: String? = null) {
    if(name != null)
        println("\n$name:")
    while(available() > 0)
        print(read().toChar())
    close()
}

fun Task.dependOnSubprojects() {
    if(this.project == rootProject)
        doAfterEvaluate.add {
            if(it != rootProject)
                dependsOn(it.tasks.findByName(name) ?: return@add)
        }
}

// "run" task won't work when recursive, see https://stackoverflow.com/q/51903863/6723250
gradle.taskGraph.whenReady {
    val hasRootRunTask = hasTask(":run")
    if(hasRootRunTask) {
        allTasks.forEach { task ->
            task.enabled = task.name != "run"
        }
    }
}
