grails.project.work.dir = "target"

grails.project.dependency.resolver="maven"
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile("org.quartz-scheduler:quartz:2.2.1") {
            excludes 'slf4j-api', 'c3p0'
        }
    }

    plugins {
        build ':release:3.0.1', ':rest-client-builder:2.0.1', {
            export = false
        }
    }
}
