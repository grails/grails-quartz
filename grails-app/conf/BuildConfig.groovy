grails.project.work.dir = "target"

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

    plugins {
        build ':release:3.0.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
