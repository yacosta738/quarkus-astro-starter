plugins {
    id("io.quarkus")
    kotlin("jvm")
}

dependencies {
    implementation(project(":port"))
    implementation(project(":domain"))
    implementation("io.quarkus:quarkus-resteasy")
}
