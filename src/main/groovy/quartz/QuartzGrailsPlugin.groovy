/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quartz

import grails.plugins.Plugin
import grails.plugins.quartz.CustomTriggerFactoryBean
import grails.plugins.quartz.GrailsJobClass
import grails.plugins.quartz.GrailsJobFactory
import grails.plugins.quartz.JobArtefactHandler
import grails.plugins.quartz.JobDetailFactoryBean
import grails.plugins.quartz.listeners.ExceptionPrinterJobListener
import grails.plugins.quartz.listeners.SessionBinderJobListener
import groovy.util.logging.Commons
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.ListenerManager
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.matchers.KeyMatcher
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.SchedulerFactoryBean

@Commons
class QuartzGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0.BUILD-SNAPSHOT > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Quartz" // Headline display name of the plugin
    def author = "Jeff Brown"
    def authorEmail = "jeff@jeffandbetsy.net"
    def description = '''\
Adds Quartz job scheduling features
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/quartz"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Any additional developers beyond the author specified above.
    def developers = [
            [ name: "Sergey Nebolsin", email: "nebolsin@gmail.com" ],
            [ name: "Graeme Rocher", email: "graeme.rocher@gmail.com" ],
            [ name: "Ryan Vanderwerf", email: "rvanderwerf@gmail.com" ],
            [ name: "Vitalii Samolovskikh", email: "kefir@perm.ru" ]
    ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/grails3-plugins/quartz/" ]

    Closure doWithSpring() {
        { ->
            boolean hasHibernate = hasHibernate(manager)

            // Configure job beans
            grailsApplication.jobClasses.each { GrailsJobClass jobClass ->
                configureJobBeans.delegate = delegate
                configureJobBeans(jobClass, hasHibernate)
            }

            // Configure the session listener if there is the Hibernate
            if (hasHibernate) {
                log.debug("Registering hibernate SessionBinderJobListener")

                // register SessionBinderJobListener to bind Hibernate Session to each Job's thread
                "${SessionBinderJobListener.NAME}"(SessionBinderJobListener) { bean ->
                    bean.autowire = "byName"
                }
            }

            // register global ExceptionPrinterJobListener which will log exceptions occured
            // during job's execution
            "${ExceptionPrinterJobListener.NAME}"(ExceptionPrinterJobListener)

            // Configure the job factory to create job instances on executions.
            quartzJobFactory(GrailsJobFactory)

            // Configure Scheduler
            configureScheduler.delegate = delegate
            configureScheduler()
        }
    }

    /**
     * Configure job beans.
     */
    def configureJobBeans = { GrailsJobClass jobClass, boolean hasHibernate = true ->
        def fullName = jobClass.fullName

        try {
            "${fullName}Class"(MethodInvokingFactoryBean) {
                targetObject = ref("grailsApplication", false)
                targetMethod = "getArtefact"
                arguments = [JobArtefactHandler.TYPE, jobClass.fullName]
            }

            "${fullName}"(ref("${fullName}Class")) { bean ->
                bean.factoryMethod = "newInstance"
                bean.autowire = "byName"
                bean.scope = "prototype"
            }
        } catch (Exception e) {
            log.error("Error declaring ${fullName}Detail bean in context", e)
        }
    }

    def configureScheduler = {  ->
        quartzScheduler(SchedulerFactoryBean) { bean ->

            // delay scheduler startup to after-bootstrap stage
            autoStartup = config.quartz.autoStartup

            // Store
            if (config.jdbcStore) {
                dataSource = ref(config.jdbcStoreDataSource ?: 'dataSource')
                transactionManager = ref('transactionManager')
            }
            waitForJobsToCompleteOnShutdown = false //config.waitForJobsToCompleteOnShutdown
            exposeSchedulerInRepository = true //config.exposeSchedulerInRepository
            jobFactory = quartzJobFactory

            // Global listeners on each job.
            globalJobListeners = [ref(ExceptionPrinterJobListener.NAME)]

            // Destroys the scheduler before the application will stop.
            bean.destroyMethod = 'destroy'
        }
    }

    /**
     * Schedules jobs. Creates job details and trigger beans. And schedules them.
     */
    def scheduleJob(GrailsJobClass jobClass, ApplicationContext ctx, boolean hasHibernate) {
        Scheduler scheduler = ctx.quartzScheduler
        if (scheduler) {
            def fullName = jobClass.fullName

            // Creates job details
            JobDetailFactoryBean jdfb = new JobDetailFactoryBean()
            jdfb.jobClass = jobClass
            jdfb.afterPropertiesSet()
            JobDetail jobDetail = jdfb.object

            // adds the job to the scheduler, and associates triggers with it
            scheduler.addJob(jobDetail, true)

            // The session listener if is needed
            if (hasHibernate && jobClass.sessionRequired) {
                SessionBinderJobListener listener = ctx.getBean(SessionBinderJobListener.NAME)
                if (listener != null) {
                    ListenerManager listenerManager = scheduler.getListenerManager()
                    KeyMatcher<JobKey> matcher = KeyMatcher.keyEquals(jobDetail.key)
                    if (listenerManager.getJobListener(listener.getName()) == null) {
                        listenerManager.addJobListener(listener, matcher)
                    } else {
                        listenerManager.addJobListenerMatcher(listener.getName(), matcher)
                    }
                } else {
                    log.error("The SessionBinderJobListener has not been initialized.")
                }
            }

            // Creates and schedules triggers
            jobClass.triggers.each { name, Expando descriptor ->
                CustomTriggerFactoryBean factory = new CustomTriggerFactoryBean()
                factory.triggerClass = descriptor.triggerClass
                factory.triggerAttributes = descriptor.triggerAttributes
                factory.jobDetail = jobDetail
                factory.afterPropertiesSet()
                Trigger trigger = factory.object

                TriggerKey key = trigger.key
                log.debug("Scheduling $fullName with trigger $key: ${trigger}")
                if (scheduler.getTrigger(key) != null) {
                    scheduler.rescheduleJob(key, trigger)
                } else {
                    scheduler.scheduleJob(trigger)
                }
                log.debug("Job ${fullName} scheduled")
            }
        } else {
            log.error("Failed to schedule job details and job triggers: scheduler not found.")
        }
    }

    void doWithApplicationContext() {
        grailsApplication.jobClasses.each { GrailsJobClass jobClass ->
            scheduleJob(jobClass, applicationContext, hasHibernate(manager))
            def clz = jobClass.clazz
            clz.scheduler = applicationContext.quartzScheduler
            clz.grailsJobClass = jobClass
        }
        log.debug("Scheduled Job Classes count: " + grailsApplication.jobClasses.size())
    }

    private boolean hasHibernate(manager) {
        manager?.hasGrailsPlugin("hibernate") || manager?.hasGrailsPlugin("hibernate4")
    }
}
