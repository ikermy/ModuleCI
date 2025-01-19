plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "ModuleCI"

// Функции для работы с версией
val versionFile = File("version.txt")

fun getVersion(): String {
    if (!versionFile.exists()) {
        versionFile.writeText("0.1.0")
    }
    return versionFile.readText().trim()
}

fun incrementVersion() {
    val versionParts = versionFile.readText().trim().split(".")
    val newPatchVersion = versionParts.last().toInt() + 1
    val newVersion = versionParts.dropLast(1) + newPatchVersion.toString()
    versionFile.writeText(newVersion.joinToString("."))
}

version = getVersion()

tasks.register("incrementVersion") {
    doLast {
        incrementVersion()
    }
}

tasks.named("build") {
    dependsOn("incrementVersion")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.hierynomus:sshj:0.39.0")
    implementation("com.typesafe:config:1.4.2")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")
}

tasks.test {
    useJUnitPlatform()
}

application { mainClass.set("ib.infobot.MainKt") }

kotlin {
    jvmToolchain(17)
}
