jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        xml.enabled.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}
tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

file("sonar-project.properties").withReader { reader ->
    val sonarProperties: Properties = Properties()
    sonarProperties.load(reader)

    sonarProperties.each { key, value ->
        sonarqube {
            properties {
                property(key.toString(), value.toString())
            }
        }
    }
}


