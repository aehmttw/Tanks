plugins {
    `java-library`
    `maven-publish`
    checkstyle
}

fun getHash(): String {
    return Runtime.getRuntime()
        .exec("git rev-parse --short HEAD")
        .inputStream
        .bufferedReader()
        .readLine()
        ?.trim() ?: "00000000"
}

val lwjglVersion = "3.4.1"
val lwjglNatives = listOf(
    "natives-freebsd",
    "natives-linux-arm32", "natives-linux-arm64",
	"natives-linux-ppc64le", "natives-linux-riscv64",
	"natives-linux",
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
    implementation("io.netty:netty-all:4.1.94.Final")
//    implementation("it.unimi.dsi:fastutil-core:8.5.16")

    //Steamworks4j (Use files in libs folder until version 10 is available on mavenCentral
    implementation("com.code-disaster.steamworks4j:steamworks4j:1.10.0")
    implementation("com.code-disaster.steamworks4j:steamworks4j-lwjgl3:1.10.0")
}

// ---------------------------------------------------------------------------
// Linting – Checkstyle
// ---------------------------------------------------------------------------

// Separate configuration that holds only the Checkstyle JAR (no custom-check
// classes output).  This is the compile classpath for our AbstractCheck subclasses
// and avoids a circular dependency: the 'checkstyle' tool configuration also
// contains checkstyleChecks.output.classesDirs, so we cannot use it here.
val checkstyleApi by configurations.creating { isTransitive = true }

// Source set that holds the two custom Checkstyle check classes.
// Sources live in src/checkstyle/java/ so they are clearly separate from
// the game code and are never included in the production JAR.
val checkstyleChecks by sourceSets.creating {
    java.srcDir("src/checkstyle/java")
    compileClasspath += checkstyleApi
}

dependencies {
    checkstyleApi("com.puppycrawl.tools:checkstyle:8.45.1")

    // Add the compiled custom-check classes to the Checkstyle tool classpath
    // so Checkstyle can load tanks.linting.ColonSpacingCheck etc.
    checkstyle(checkstyleChecks.output.classesDirs)
    checkstyle("com.puppycrawl.tools:checkstyle:8.45.1")
}

checkstyle {
    toolVersion = "8.45.1"
    configFile  = file("config/checkstyle/checkstyle.xml")
    // Empty source sets: prevents the plugin from auto-wiring checkstyleMain
    // into the check/build lifecycle. Linting only runs via `./gradlew lint`.
    sourceSets  = emptyList()
    isIgnoreFailures = false
    isShowViolations = false
    maxWarnings = 0
}

// Custom check classes must be compiled before any Checkstyle task runs.
tasks.withType<Checkstyle>().configureEach {
    dependsOn(tasks.named(checkstyleChecks.compileJavaTaskName))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Convenience alias: `./gradlew lint`
tasks.register("lint") {
    description = "Runs Checkstyle linting on the main source set."
    group       = "verification"
    dependsOn("checkstyleMain")
}

// ---------------------------------------------------------------------------

group = "com.aehmttw"
version = rootProject.file("src/main/resources/version.txt").readText().trim()
rootProject.file("src/main/resources/hash.txt").writeText(getHash())
description = "Tanks"
java.sourceCompatibility = JavaVersion.VERSION_1_8

// Force UTF-8 everywhere
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}


tasks.withType<Test>().configureEach {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.jar {
    archiveBaseName.set("Tanks")
    archiveVersion.set("${version}-${getHash()}")
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
