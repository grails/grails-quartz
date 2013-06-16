/*
 * Copyright (c) 2011 the original author or authors.
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


import grails.plugins.quartz.GrailsJobClass
import grails.plugins.quartz.GrailsJobClassConstants as Constants
import grails.plugins.quartz.GrailsJobFactory
import grails.plugins.quartz.JobArtefactHandler
import grails.plugins.quartz.JobDetailFactoryBean
import grails.plugins.quartz.listeners.ExceptionPrinterJobListener
import grails.plugins.quartz.listeners.SessionBinderJobListener
import grails.util.Environment
import org.quartz.*
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.SchedulerFactoryBean

import static grails.plugins.quartz.TriggerUtils.*

/**
 * A plug-in that configures Quartz job support for Grails.
 *
 *
 * @author Graeme Rocher
 * @author Marcel Overdijk
 * @author Sergey Nebolsin
 * @author Ryan Vanderwerf
 */
class QuartzGrailsPlugin {

    def version = "1.0-SNAPSHOT"
    def grailsVersion = "2.0 > *"

    def author = "Sergey Nebolsin, Graeme Rocher, Ryan Vanderwerf"
    def authorEmail = "rvanderwerf@gmail.com"
    def title = "Quartz plugin for Grails"
    def description = '''\
This plugin adds Quartz job scheduling features to Grails application.
'''
    def documentation = "http://grails.org/plugin/quartz"
    def pluginExcludes = ['grails-app/jobs/**']

    def license = "APACHE"
    def issueManagement = [system: "GitHub Issues", url: "http://jira.grails.org/browse/GPQUARTZ"]
    def scm = [url: "http://github.com/grails-plugins/grails-quartz"]

    def loadAfter = ['core', 'hibernate', 'datasources']
    def watchedResources = [
            "file:./grails-app/jobs/**/*Job.groovy",
            "file:./plugins/*/grails-app/jobs/**/*Job.groovy"
    ]

    def artefacts = [new JobArtefactHandler()]

    /**
     * Configures The Spring context.
     */
    def doWithSpring = { context ->
        def config = loadQuartzConfig(application.config)

        application.jobClasses.each { jobClass ->
            configureJobBeans.delegate = delegate
            configureJobBeans(jobClass, manager.hasGrailsPlugin("hibernate"))
        }

        if (manager?.hasGrailsPlugin("hibernate")) {
            // register SessionBinderJobListener to bind Hibernate Session to each Job's thread
            "${SessionBinderJobListener.NAME}"(SessionBinderJobListener) { bean ->
                bean.autowire = "byName"
            }
        }

        // register global ExceptionPrinterJobListener which will log exceptions occured
        // during job's execution
        "${ExceptionPrinterJobListener.NAME}"(ExceptionPrinterJobListener)

        quartzJobFactory(GrailsJobFactory)

        quartzScheduler(SchedulerFactoryBean) {
            quartzProperties = config._properties

            // Use the instanceName property to set the name of the scheduler
            if (quartzProperties['org.quartz.scheduler.instanceName']) {
                schedulerName = quartzProperties['org.quartz.scheduler.instanceName']
            }

            // delay scheduler startup to after-bootstrap stage
            autoStartup = false
            if (config.jdbcStore) {
                dataSource = ref('dataSource')
                transactionManager = ref('transactionManager')
            }
            waitForJobsToCompleteOnShutdown = config.waitForJobsToCompleteOnShutdown
            exposeSchedulerInRepository = config.exposeSchedulerInRepository
            jobFactory = quartzJobFactory
            if (manager?.hasGrailsPlugin("hibernate")) {
                globalJobListeners = [ref("${SessionBinderJobListener.NAME}"), ref("${ExceptionPrinterJobListener.NAME}")]
            } else {
                globalJobListeners = [ref("${ExceptionPrinterJobListener.NAME}")]
            }
        }

    }

    /**
     * Adds schedule methods for job classes.
     */
    private void addMethods(tc, ctx) {
        Scheduler quartzScheduler = ctx.getBean('quartzScheduler')
        def mc = tc.metaClass
        String jobName = tc.getFullName()
        String jobGroup = tc.getGroup()

        def scheduleTrigger = { Trigger trigger, Map params = null ->
            if (params) {
                trigger.jobDataMap.putAll(params)
            }
            quartzScheduler.scheduleJob(trigger)
        }

        // Schedule with job with cron trigger
        mc.'static'.schedule = { String cronExpression, Map params = null ->
            scheduleTrigger(buildCronTrigger(jobName, jobGroup, cronExpression), params)
        }

        // Schedule the job with simple trigger
        mc.'static'.schedule = {
            Long repeatInterval, Integer repeatCount = SimpleTrigger.REPEAT_INDEFINITELY, Map params = null ->
                scheduleTrigger(buildSimpleTrigger(jobName, jobGroup, repeatInterval, repeatCount), params)
        }

        // Schedule the job at specified time
        mc.'static'.schedule = { Date scheduleDate, Map params = null ->
            scheduleTrigger(buildDateTrigger(jobName, jobGroup, scheduleDate), params)
        }

        // Schedule the job with trigger
        mc.'static'.schedule = { Trigger trigger, Map params = null ->
            if (params) trigger.jobDataMap.putAll(params)
            quartzScheduler.scheduleJob(quartzScheduler.getJobDetail(new JobKey(jobName, jobGroup)), trigger)
        }

        mc.'static'.triggerNow = { Map params = null ->
            quartzScheduler.triggerJob(new JobKey(jobName, jobGroup), params ? new JobDataMap(params) : null)
        }

        mc.'static'.removeJob = {
            quartzScheduler.deleteJob(new JobKey(jobName, jobGroup))
        }

        mc.'static'.reschedule = { Trigger trigger, Map params = null ->
            if (params) trigger.jobDataMap.putAll(params)
            quartzScheduler.rescheduleJob(trigger.key, trigger)
        }

        mc.'static'.unschedule = { String triggerName, String triggerGroup = Constants.DEFAULT_TRIGGERS_GROUP ->
            quartzScheduler.unscheduleJob(TriggerKey.triggerKey(triggerName, triggerGroup))
        }
    }

    def doWithDynamicMethods = { ctx ->
        application.jobClasses.each { GrailsJobClass tc ->
            addMethods(tc, ctx)
        }
    }

    def doWithApplicationContext = { applicationContext ->
        application.jobClasses.each { jobClass ->
            scheduleJob.delegate = delegate
            scheduleJob(jobClass, applicationContext)
        }
        log.debug("Scheduled Job Classes Count:" + application.jobClasses.size())
    }

    def onChange = { event ->
        if (application.isArtefactOfType(JobArtefactHandler.TYPE, event.source)) {
            log.debug("Job ${event.source} changed. Reloading...")
            def context = event.ctx
            def scheduler = context?.getBean("quartzScheduler")

            def jobClass = application.getJobClass(event.source?.name)

            if (context && jobClass) {
                addMethods(jobClass, context)
            }

            // get quartz scheduler
            if (context && scheduler) {
                // if job already exists, delete it from scheduler                
                if (jobClass) {
                    def jobKey = new JobKey(jobClass.fullName, jobClass.group)
                    scheduler.deleteJob(jobKey)
                    log.debug("Job ${jobClass.fullName} deleted from the scheduler")
                }

                // add job artefact to application
                jobClass = application.addArtefact(JobArtefactHandler.TYPE, event.source)

                // configure and register job beans
                def fullName = jobClass.fullName
                def beans = beans {
                    configureJobBeans.delegate = delegate
                    configureJobBeans(jobClass, manager.hasGrailsPlugin("hibernate"))
                }

                context.registerBeanDefinition("${fullName}Class", beans.getBeanDefinition("${fullName}Class"))
                context.registerBeanDefinition("${fullName}", beans.getBeanDefinition("${fullName}"))
                context.registerBeanDefinition("${fullName}Detail", beans.getBeanDefinition("${fullName}Detail"))

                jobClass.triggers.each { name, trigger ->
                    event.ctx.registerBeanDefinition("${name}Trigger", beans.getBeanDefinition("${name}Trigger"))
                }

                scheduleJob(jobClass, event.ctx)
            } else {
                log.error("Application context or Quartz Scheduler not found. Can't reload Quartz plugin.")
            }
        }
    }

    def scheduleJob = { GrailsJobClass jobClass, ApplicationContext ctx ->
        Scheduler scheduler = ctx.getBean("quartzScheduler") as Scheduler
        if (scheduler) {
            def fullName = jobClass.fullName
            // add job to scheduler, and associate triggers with it
            if (ctx.getBean("${fullName}Detail")) {
                scheduler.addJob(ctx.getBean("${fullName}Detail"), true)
                jobClass.triggers.each { key, trigger ->
                    TriggerKey triggerKey = new TriggerKey(trigger.triggerAttributes.name, trigger.triggerAttributes.group)
                    log.debug("Scheduling $fullName with trigger $key: ${trigger}")
                    if (scheduler.getTrigger(triggerKey)) {
                        scheduler.rescheduleJob(triggerKey, ctx.getBean("${key}Trigger"))
                    } else {
                        scheduler.scheduleJob(ctx.getBean("${key}Trigger"))
                    }
                }
                log.debug("Job ${jobClass.fullName} scheduled")
            } else {
                log.error("Error scheduling job, ${fullName}Detail not found in ApplicationContext!")
            }
        } else {
            log.error("Failed to register job triggers: scheduler not found")
        }
    }

    def configureJobBeans = { GrailsJobClass jobClass, boolean hasHibernate = true ->

        def fullName = jobClass.fullName

        try {


            "${fullName}Class"(MethodInvokingFactoryBean) {
                targetObject = ref("grailsApplication", true)
                targetMethod = "getArtefact"
                arguments = [JobArtefactHandler.TYPE, jobClass.fullName]
            }

            "${fullName}"(ref("${fullName}Class")) { bean ->
                bean.factoryMethod = "newInstance"
                bean.autowire = "byName"
                bean.scope = "prototype"
            }

            "${fullName}Detail"(JobDetailFactoryBean) {
                name = fullName
                group = jobClass.group
                concurrent = jobClass.concurrent
                durability = jobClass.durability
                requestsRecovery = jobClass.requestsRecovery

                if (hasHibernate && jobClass.sessionRequired) {
                    jobListenerNames = ["${SessionBinderJobListener.NAME}"] as String[]
                }
            }
        } catch (Exception e) {
            log.error("Error declaring ${fullName}Detail bean in context", e)
        }

        // registering triggers
        try {
            jobClass.triggers.each { name, trigger ->
                "${name}Trigger"(trigger.clazz) {
                    jobDetail = ref("${fullName}Detail")
                    trigger.properties.findAll { it.key != 'clazz' }.each {
                        delegate["${it.key}"] = it.value
                    }
                }
            }
        } catch (Exception te) {
            log.error("Error registering triggers", te)
        }
    }

    /*
     * Load the various configs. 
     * Order of priority has been "fixed" in 1.0-RC2 to be:
     *
     * 1. DefaultQuartzConfig is loaded 
     * 2. App's Config.groovy is loaded in and overwrites anything from DQC
     * 3. QuartzConfig is loaded and overwrites anything from DQC or AppConfig
     * 4. quartz.properties are loaded into config as quartz._props
     */

    private ConfigObject loadQuartzConfig(ConfigObject config) {
        def classLoader = new GroovyClassLoader(getClass().classLoader)
        String environment = Environment.current.toString()

        // Note here the order of objects when calling merge - merge OVERWRITES values in the target object
        // Load default Quartz config as a basis
        def newConfig = new ConfigSlurper(environment).parse(
                classLoader.loadClass('DefaultQuartzConfig')
        )

        // Overwrite defaults with what Config.groovy has supplied, perhaps from external files
        newConfig.merge(config)

        // Overwrite with contents of QuartzConfig
        try {
            newConfig.merge(new ConfigSlurper(environment).parse(
                    classLoader.loadClass('QuartzConfig'))
            )
        } catch (Exception ignored) {
            // ignore, just use the defaults
        }

        // Now merge our correctly merged DefaultQuartzConfig and QuartzConfig into the main config
        config.merge(newConfig)

        // And now load quartz properties into main config
        def properties = new Properties()
        def resource = classLoader.getResourceAsStream("quartz.properties")
        if (resource != null) {
            properties.load(resource)
        }

        if (config.quartz.containsKey('props')) {
            properties << config.quartz.props.toProperties('org.quartz')
        }

        config.quartz._properties = properties

        return config.quartz
    }
}
