import com.github.gradle.node.util.main
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Date
val nodeVersion: String by project
val npmVersion: String by project
plugins {
    id("io.quarkus")
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.liquibase.gradle")
    id("com.github.node-gradle.node")
}

val spa = "${project.projectDir}/src/main/webapp"
println("frontend: $spa")

node {
    version.set(nodeVersion)
    npmVersion.set(npmVersion)
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
    implementation("org.mapstruct:mapstruct-processor:1.5.5.Final")
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

defaultTasks("quarkusDev")
tasks.test { onlyIf { !project.hasProperty("skipTests") } }
tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
    exclude("**/*IT*", "**/*IntTest*")
    reports.html.required.set(false)
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    include("**/*IT*", "**/*IntTest*")
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
    shouldRunAfter("test")
    reports.html.required.set(false)
}

tasks.check { dependsOn(integrationTest) }
integrationTest.onlyIf { !project.hasProperty("skipTests") }

val testReport = tasks.register<TestReport>("testReport") {
    destinationDir = file("${buildDir}/reports/tests")
    reportOn(tasks.named("test"))
}
val integrationTestReport = tasks.register<TestReport>("integrationTestReport") {
    destinationDir = file("${buildDir}/reports/integrationTests")
    reportOn(integrationTest)
}

if (!project.hasProperty("runList")) {
    project.ext.set("runList", "main")
}
val now = Date()
project.ext.set("diffChangelogFile", "${project.projectDir}/src/main/resources/config/liquibase/changelog/${now.time}_changelog.xml")
liquibase {
    activities.register("main") {
        val db_url by project.extra.properties
        val db_user by project.extra.properties
        val db_pass by project.extra.properties
        this.arguments = mapOf(
            "logLevel" to "debug",
            "changeLogFile" to project.ext.get("diffChangelogFile"),
            "url" to db_url,
            "username" to db_user,
            "password" to db_pass
        )
    }
    runList = project.ext.get("runList") as String
}

val buildWeb = tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildNpm") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
//    args.set(listOf("--", "--prod"))
    inputs.dir("${spa}/src")
    inputs.dir(fileTree("${spa}/node_modules").exclude("${spa}/.cache"))
    outputs.dir("${spa}/dist")
}
