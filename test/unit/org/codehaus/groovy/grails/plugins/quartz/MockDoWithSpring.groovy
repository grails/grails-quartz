package org.codehaus.groovy.grails.plugins.quartz

import org.codehaus.groovy.grails.plugins.quartz.listeners.SessionBinderJobListener
import org.codehaus.groovy.grails.plugins.quartz.listeners.ExceptionPrinterJobListener

class MockDoWithSpring {

    def quartzProperties
    def application = [jobClasses: null]
    def manager

    def ref( def whatever ) {
        null
    }

    def quartzJobFactory( def whatever ) {
        null
    }

    def exceptionPrinterListener( def whatever ) {
        null
    }

    def sessionBinderListener( def something, def whatever ) {
        null
    }

    void quartzScheduler( def whatever, Closure props ) {
        def data = [:]
        props.delegate = data
        props.resolveStrategy = Closure.DELEGATE_FIRST
        props.call()
        println "xxxxxxxx=$data"
        this.quartzProperties = data.quartzProperties
    }
}