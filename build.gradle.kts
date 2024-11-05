plugins {
    `java-library`
    `maven-publish`
}

val lwjglVersion = "3.3.4"
val lwjglNatives = listOf(
    "natives-freebsd",
    "natives-linux-arm32", "natives-linux-arm64", "natives-linux-ppc64le", "natives-linux-riscv64", "natives-linux",
    "natives-macos", "natives-macos-arm64",
    "natives-windows-x86", "natives-windows", "natives-windows-arm64",
)

repositories {
    mavenCentral()
    mavenLocal()

}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")

    for (native in lwjglNatives) {
        runtimeOnly("org.lwjgl", "lwjgl", classifier = native)
        runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = native)
        runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = native)
        runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = native)
        runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = native)
        runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = native)
    }

    // Your other existing dependencies
    api(libs.org.l33tlabs.twl.pngdecoder)
    api(libs.org.apache.commons.commons.io)
    implementation("io.netty:netty-all:4.1.68.Final")

    //Steamworks
    api(files("libs/steamworks4j-1.10.0-SNAPSHOT.jar"))
    api(files("libs/steamworks4j-lwjgl3-1.10.0-SNAPSHOT.jar"))
}

group = "com.aehmttw"
version = rootProject.file("src/main/resources/version.txt").readText().toString().trim()
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

tasks.jar {
    archiveBaseName.set("Tanks")
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

task("BuildMacApp", Exec::class) {
    dependsOn(tasks.jar)

    val distributions = file("build/distributions")
    val jpackage = org.gradle.internal.jvm.Jvm.current().javaHome.resolve("bin/jpackage")
    val libsDir = file("${layout.buildDirectory.get()}").resolve("libs")
    val jarName = tasks.named<Jar>("jar").get().archiveFileName.get()
    val baseName = tasks.named<Jar>("jar").get().archiveBaseName.get()
    val resourcesDir = sourceSets.main.get().resources.srcDirs.first()

    workingDir = projectDir

    delete(distributions.resolve("Tanks.app"))

    commandLine(
        jpackage,
        "--type", "app-image",
        "--input", libsDir,
        "--main-jar", jarName,
        "--name", baseName,
        "--resource-dir", resourcesDir,
        "--java-options", "-XstartOnFirstThread",
        "--arguments", "mac",
        "--dest", distributions,
    )
}