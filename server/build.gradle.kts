import java.util.Scanner

plugins {
    application
}

sourceSets {
    main.get().java.srcDir("src")
    test.get().java.srcDir("test")
}

application {
    mainClassName = "sc.server.Application"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8",
            "-XX:+PrintGC", "-XX:+PrintGCDetails", "-XX:+PrintGCDateStamps", "-Xloggc:gc.log")
}

dependencies {
    implementation(project(":sdk"))

    testImplementation("junit", "junit", "4.12")
}

val deployDir: File by project

tasks {
    val runnableDir = buildDir.resolve("runnable")

    val createScripts by creating(ScriptsTask::class) {
        destinationDir = runnableDir
        fileName = "start"
        content = "java -Dfile.encoding=UTF-8 -Dlogback.configurationFile=logback.xml -jar server.jar"
    }

    val copyPlugin by creating(Copy::class) {
        dependsOn(project(":plugin").tasks.jar.get())
        from(project(":plugin").buildDir.resolve("libs"))
        into(runnableDir.resolve("plugins"))
        rename("server.jar", "software-challenge-server.jar")
        doLast {
            copy {
                from("configuration/logback-release.xml", "configuration/server.properties.example")
                into(runnableDir)
                rename("logback-release.xml", "logback.xml")
                rename("server.properties.example", "server.properties")
            }
        }
    }

    val makeRunnable by creating(Copy::class) {
        dependsOn(jar.get(), copyPlugin, createScripts)
        from(configurations.default)
        into(runnableDir.resolve("lib"))
    }

    val deploy by creating(Zip::class) {
        dependsOn(":test-client:jar", ":player:shadowJar", "makeRunnable")
        destinationDirectory.set(deployDir)
        archiveBaseName.set("software-challenge-server")
        from(project(":test-client").buildDir.resolve("libs"), project(":player").buildDir.resolve("libs"), runnableDir)
        doFirst {
            runnableDir.resolve("version").writeText(Scanner(Runtime.getRuntime().exec("git rev-parse HEAD").inputStream).next())
        }
    }

    val startProduction by creating(JavaExec::class) {
        dependsOn(makeRunnable)
        classpath = files(configurations.default, runnableDir.resolve("software-challenge-server.jar"))
        main = "sc.server.Application"
        workingDir = runnableDir
        jvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dlogback.configurationFile=../../logback-production.xml", "-Djava.security.egd=file:/dev/./urandom", "-XX:MaxGCPauseMillis=100", "-XX:GCPauseIntervalMillis=2050", "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled", "-XX:+UseCMSInitiatingOccupancyOnly", "-XX:CMSInitiatingOccupancyFraction=70", "-XX:+ScavengeBeforeFullGC", "-XX:+CMSScavengeBeforeRemark")
    }

    val dockerImage by creating(Exec::class) {
        dependsOn(makeRunnable)
        val tag = Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short", "--verify", "HEAD")).inputStream.reader().readText().trim()
        val relativeRunnable = runnableDir.relativeTo(project.projectDir)
        commandLine("docker", "build", "--no-cache", "-t", "swc_game-server:latest", "-t", "swc_game-server:$tag", "--build-arg", "game_server_dir=$relativeRunnable", ".")
    }

    jar {
        destinationDirectory.set(runnableDir)
        manifest.attributes["Class-Path"] = configurations.default.get().joinToString(" ") { "lib/" + it.name }
    }

    run.configure {
        dependsOn(copyPlugin)
        workingDir = runnableDir
        jvmArgs = listOf("-Dlogback.configurationFile=../../configuration/logback.xml")
        args = System.getProperty("args", "").split(" ")
    }

}
