import org.gradle.internal.os.OperatingSystem

plugins {
    `java-library`
    `maven-publish`
}

val lwjglVersion = "3.3.4"
val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> {
        val osArch = System.getProperty("os.arch")
        when {
            osArch.startsWith("arm") || osArch.startsWith("aarch64") ->
                "natives-linux${if (osArch.contains("64") || osArch.startsWith("armv8")) "-arm64" else "-arm32"}"
            osArch.startsWith("ppc") -> "natives-linux-ppc64le"
            osArch.startsWith("riscv") -> "natives-linux-riscv64"
            else -> "natives-linux"
        }
    }
    OperatingSystem.MAC_OS -> if (System.getProperty("os.arch").startsWith("aarch64")) "natives-macos-arm64" else "natives-macos"
    OperatingSystem.WINDOWS -> {
        val osArch = System.getProperty("os.arch")
        if (osArch.contains("64")) {
            if (osArch.startsWith("aarch64")) "natives-windows-arm64" else "natives-windows"
        } else "natives-windows-x86"
    }
    OperatingSystem.FREE_BSD -> "natives-freebsd"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    // Your other existing dependencies
    api(libs.org.l33tlabs.twl.pngdecoder)
    api(libs.org.apache.commons.commons.io)
    implementation("io.netty:netty-all:4.1.68.Final")
    implementation("com.google.code.gson:gson:2.11.0")

    //Steamworks
    api(files("libs/steamworks4j-1.10.0-SNAPSHOT.jar"))
    api(files("libs/steamworks4j-lwjgl3-1.10.0-SNAPSHOT.jar"))
}

group = "com.aehmttw"
version = rootProject.file("version.txt").readText().toString().trim()
description = "Tanks"
java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.jar {
    archiveBaseName.set("Tanks")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        from("src/main/java/META-INF/MANIFEST.MF")
    }

    // If you have dependencies that need to be included in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.register<JavaExec>("run") {
    description = "Runs the JAR file"
    group = "application" // This puts the task in the "application" group in Gradle tasks list

    // Ensure the JAR is built before trying to run it
    dependsOn(tasks.jar)

    // Use the JAR file as the classpath
    classpath(tasks.jar.get().outputs.files)

    // Set the main class to run
    // Replace "com.aehmttw.tanks.Main" with your actual main class
    mainClass.set("main.Tanks")

    // If your application needs working directory to be set
    workingDir = project.projectDir
}