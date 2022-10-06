dependencies {

}
var profiles = "prod"
if (project.hasProperty("no-liquibase")) {
    profiles += ",no-liquibase"
}

if (project.hasProperty("swagger")) {
    profiles += ",swagger"
}
