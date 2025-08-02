plugins {
    `java-library`
    `maven-publish`
}

fun getHash(): String {
    return Runtime.getRuntime()
        .exec("git rev-parse --short HEAD")
        .inputStream
        .bufferedReader()
        .readLine()
        ?.trim() ?: "00000000"
}

val lwjglVersion = "3.3.3"
val lwjglNatives = listOf(
    //"natives-freebsd",
    "natives-linux-arm32", "natives-linux-arm64", 
	//"natives-linux-ppc64le", "natives-linux-riscv64", 
	"natives-linux",
    "natives-macos", "natives-macos-arm64",
    "natives-windows-x86", "natives-windows", "natives-windows-arm64",
)

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
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
    implementation("io.netty:netty-all:4.1.94.Final")
    implementation("it.unimi.dsi:fastutil-core:8.5.16")
    api("com.code-disaster.steamworks4j:steamworks4j:1.10.0-SNAPSHOT")
    api("com.code-disaster.steamworks4j:steamworks4j-lwjgl3:1.10.0-SNAPSHOT")
}

group = "com.aehmttw"
version = rootProject.file("src/main/resources/version.txt").readText().trim()
rootProject.file("src/main/resources/hash.txt").writeText(getHash())
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

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

task("run", type = JavaExec::class) {
    description = "Runs the JAR file"
    group = "application"
    dependsOn(tasks.jar)
    classpath(tasks.jar.get().outputs.files)
    mainClass.set("main.Tanks")
    workingDir = project.projectDir
    args("debug")
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
