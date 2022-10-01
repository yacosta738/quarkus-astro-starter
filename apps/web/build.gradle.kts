plugins {
    id("io.quarkus")
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.github.node-gradle.node")
}

val spa = "${project.projectDir}/src/main/webapp"
println("frontend: $spa")
node {
    version.set("16.17.1")
    npmInstallCommand.set("install")
    download.set(true)
    workDir.set(file("${project.projectDir}/.cache/node"))
    npmWorkDir.set(file("${project.projectDir}/.cache/npm"))
    yarnWorkDir.set(file("${project.projectDir}/.cache/yarn"))
    nodeProjectDir.set(file(spa))
    nodeProxySettings.set(com.github.gradle.node.npm.proxy.ProxySettings.SMART)
}

dependencies {
    implementation(project(":port"))
    implementation(project(":domain"))
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-elytron-security-jdbc")
    implementation("io.quarkus:quarkus-resteasy-jsonb")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-elytron-security")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("org.mapstruct:mapstruct-processor:1.5.2.Final")
    implementation("com.tietoevry.quarkus:quarkus-resteasy-problem:2.0.1")


    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("org.apache.commons:commons-lang3:3.12.0")

    annotationProcessor("io.quarkus:quarkus-panache-common")

    implementation(platform("org.testcontainers:testcontainers-bom:1.17.4"))
    testImplementation("org.testcontainers:postgresql")

}
allOpen {
    annotation("javax.persistence.Entity")
}

val buildWeb = tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildNpm") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
//    args.set(listOf("--", "--prod"))
    inputs.dir("${spa}/src")
    inputs.dir(fileTree("${spa}/node_modules").exclude("${spa}/.cache"))
    outputs.dir("${spa}/dist")
}
