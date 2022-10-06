val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val checkstyleVersion: String by project

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("idea")
    id("checkstyle")
    id("jacoco")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
    id("net.ltgt.apt")
    id("org.liquibase.gradle")
    id("org.sonarqube")
}
project.apply{
//    from("gradle/sonar.gradle.kts")
    if (project.hasProperty("prod")){
        from("./gradle/profile_prod.gradle.kts")
    } else{
        from("./gradle/profile_dev.gradle.kts")
    }
}

allprojects {
    group = "com.quarkus.astro"
    version = "1.0.0-SNAPSHOT"
    description = "Quarkus + Astro \uD83D\uDE80 starter project"

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("io.quarkus:quarkus-kotlin")
        testImplementation("io.quarkus:quarkus-junit5")
        testImplementation("io.rest-assured:kotlin-extensions")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        assert(
            System.getProperties()["java.specification.version"] == "17"
                || System.getProperties()["java.specification.version"] == "18"
                || System.getProperties()["java.specification.version"] == "19"
        ) {
            "Java 17 or higher is required to compile this project"
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        kotlinOptions.javaParameters = true
    }
}
idea{
    module {
        excludeDirs = excludeDirs + files("node_modules")
    }
}

eclipse {
    sourceSets {
        main {
            java {
                srcDirs += file("build/generated/sources/annotationProcessor/java/main")
            }
        }
    }
}

checkstyle {
    toolVersion = checkstyleVersion
    configFile = file("checkstyle.xml")
}
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
