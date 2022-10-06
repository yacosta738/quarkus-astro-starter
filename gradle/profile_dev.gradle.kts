//configurations {
//    developmentOnly
//    runtimeClasspath {
//        extendsFrom(developmentOnly)
//    }
//}

dependencies {

}

var profiles = "dev"
if (project.hasProperty("no-liquibase")) {
    profiles += ",no-liquibase"
}

if (project.hasProperty("tls")) {
    profiles += ",tls"
}

//processResources {
//    inputs.property("version", version)
//}
