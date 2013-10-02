grails.project.source.level = 1.6
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

    plugins {
        build ":release:3.0.1", { export = false }
    }

    dependencies {
        compile("org.quartz-scheduler:quartz:2.2.0") {
            excludes 'slf4j-api', 'c3p0'
        }
    }
}
