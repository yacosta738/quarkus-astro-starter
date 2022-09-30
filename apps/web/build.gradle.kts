plugins {
    id("io.quarkus")
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
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
}
allOpen {
    annotation("javax.persistence.Entity")
}
