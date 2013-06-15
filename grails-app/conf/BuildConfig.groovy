grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    inherits "global"
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile("org.quartz-scheduler:quartz:2.1.5") {
            excludes 'slf4j-api', 'c3p0', 'jta'
        }
    }
}
