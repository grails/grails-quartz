package org.codehaus.groovy.grails.plugins.quartz

import org.quartz.Job
import org.quartz.JobExecutionContext
/**
 * FooJob Used to make some test for Quartz Mnagement Web Application
 *
 * @author Marco Mornati (mmornati@byte-code.com)
 *
 * @since 0.4
 */
class FooJob implements Job{

    public void execute(JobExecutionContext jobExecutionContext) {
        println "Executing: " + jobExecutionContext.getJobDetail().getFullName()
        sleep (10000)
    }

}