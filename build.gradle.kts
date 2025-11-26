plugins {
    id("java")
    id("io.freefair.lombok") version "8.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm")
}

val pluginName = "NekoThePitPremium"
val version = "3.93"
repositories {
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()

    maven("https://repo.crazycrew.us/releases")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.inventivetalent.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.panda-lang.org/releases")
    mavenLocal()
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    compileOnly("redis.clients:jedis:2.9.0")
    compileOnly("org.java-websocket:Java-WebSocket:1.6.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
    api(libs.slf4j)
    api(libs.book)
    api(libs.slf4j)
    api(libs.reflectionhelper)
    compileOnly(libs.luckperms)
    implementation(libs.narshorn)
    compileOnly(libs.httpclient)
    compileOnly(libs.httpcore)
    compileOnly(libs.decentholograms)
    compileOnly(libs.protocollib)
    compileOnly(libs.papi)
    compileOnly(libs.playerpoints)
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    archiveFileName.set("$pluginName-$version.jar")
    mergeServiceFiles()
    exclude("META-INF/**")

}
tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xmx2048m")
    }
}

kotlin {
    jvmToolchain(21)
}