/*
 * Copyright 2004-2005 the original author or authors.
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

import org.codehaus.groovy.grails.plugins.quartz.*
import org.codehaus.groovy.grails.plugins.quartz.listeners.*
import org.codehaus.groovy.grails.commons.*
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import grails.util.GrailsUtil

/**
 * A plug-in that configures Quartz job support for Grails.
 *
 *
 * @author Graeme Rocher
 * @author Marcel Overdijk
 * @author Sergey Nebolsin
 */
class QuartzGrailsPlugin {

    def version = "0.3.2-SNAPSHOT"
    def author = "Sergey Nebolsin"
    def authorEmail = "nebolsin@gmail.com"
    def title = "This plugin adds Quartz job scheduling features to Grails application."
    def description = '''\
Quartz plugin allows your Grails application to schedule jobs to be
executed using a specified interval or cron expression. The underlying
system uses the Quartz Enterprise Job Scheduler configured via Spring,
but is made simpler by the coding by convention paradigm.
'''
    def documentation = "http://grails.org/Quartz+plugin"

    def loadAfter = ['core', 'hibernate']
    def watchedResources = [
            "file:./grails-app/jobs/**/*Job.groovy",
            "file:./plugins/*/grails-app/jobs/**/*Job.groovy"
    ]
    
    def artefacts = [new TaskArtefactHandler()]

    def doWithSpring = {

        def config = loadQuartzConfig()

        application.taskClasses.each {jobClass ->
            configureJobBeans.delegate = delegate
            configureJobBeans(jobClass)
        }

        // register SessionBinderJobListener to bind Hibernate Session to each Job's thread
        "${SessionBinderJobListener.NAME}"(SessionBinderJobListener) {bean ->
            bean.autowire = "byName"
        }

        // register global ExceptionPrinterJobListener which will log exceptions occured
        // during job's execution
        "${ExceptionPrinterJobListener.NAME}"(ExceptionPrinterJobListener)

        quartzScheduler(SchedulerFactoryBean) {
            // delay scheduler startup to after-bootstrap stage
            autoStartup = false
            if(config.jdbcStore) {
                dataSource = ref('dataSource')
                transactionManager = ref('transactionManager')
            }

            jobListeners = [ref("${SessionBinderJobListener.NAME}")]
            globalJobListeners = [ref("${ExceptionPrinterJobListener.NAME}")]
        }
    }

    def doWithApplicationContext = {applicationContext ->
        application.taskClasses.each {jobClass ->
            scheduleJob.delegate = delegate
            scheduleJob(jobClass, applicationContext)
        }
    }

    def onChange = {event ->
        if(application.isArtefactOfType(TaskArtefactHandler.TYPE, event.source)) {
            log.debug("Job ${event.source} changed. Reloading...")
            def context = event.ctx
            if(!context) {
                log.debug("Application context not found. Can't reload")
                return
            }

            // get quartz scheduler
            def scheduler = context.getBean("quartzScheduler")
            if(scheduler) {
                // if job already exists, delete it from scheduler
                def jobClass = application.getTaskClass(event.source?.name)
                if(jobClass) {
                    scheduler.deleteJob(jobClass.fullName, jobClass.group)
                    log.debug("Job ${jobClass.fullName} deleted from scheduler")
                }

                // add job artefact to application
                jobClass = application.addArtefact(TaskArtefactHandler.TYPE, event.source)

                // configure and register job beans
                def fullName = jobClass.fullName
                def beans = beans {
                    configureJobBeans.delegate = delegate
                    configureJobBeans(jobClass)
                }
                event.ctx.registerBeanDefinition("${fullName}JobClass", beans.getBeanDefinition("${fullName}JobClass"))
                event.ctx.registerBeanDefinition("${fullName}", beans.getBeanDefinition("${fullName}"))
                event.ctx.registerBeanDefinition("${fullName}JobDetail", beans.getBeanDefinition("${fullName}JobDetail"))
                jobClass.triggers.each {name, trigger ->
                    event.ctx.registerBeanDefinition("${name}Trigger", beans.getBeanDefinition("${name}Trigger")) 
                }

                scheduleJob(jobClass, event.ctx)
            }
        }
    }

    def scheduleJob = {GrailsTaskClass jobClass, ctx ->
        def scheduler = ctx.getBean("quartzScheduler")
        if(scheduler) {
            def fullName = jobClass.fullName
            // add job to scheduler, and associate triggers with it
            scheduler.addJob(ctx.getBean("${fullName}JobDetail"), true)
            jobClass.triggers.each {key, trigger ->
                log.debug("Scheduling $fullName with trigger $key: ${trigger}")
                scheduler.scheduleJob(ctx.getBean("${key}Trigger"))
            }
            log.debug("Job ${jobClass.fullName} scheduled")
        } else {
            log.warn("failed to register job triggers: scheduler not found")
        }
    }

    def configureJobBeans = {GrailsTaskClass jobClass ->
        def fullName = jobClass.fullName

        "${fullName}JobClass"(MethodInvokingFactoryBean) {
            targetObject = ref("grailsApplication", true)
            targetMethod = "getArtefact"
            arguments = [TaskArtefactHandler.TYPE, fullName]
        }

        "${fullName}"(ref("${fullName}JobClass")) {bean ->
            bean.factoryMethod = "newInstance"
            bean.autowire = "byName"
            bean.scope = "prototype"
        }

        "${fullName}JobDetail"(JobDetailFactoryBean) {
            grailsJobName = fullName
            concurrent = jobClass.concurrent
            group = jobClass.group
            name = fullName
            if(jobClass.sessionRequired) {
                jobListenerNames = ["${SessionBinderJobListener.NAME}"] as String[]
            }
        }

        // registering triggers
        jobClass.triggers.each {name, trigger ->
            "${name}Trigger"(trigger.clazz) {
                jobDetail = ref("${fullName}JobDetail")
                trigger.properties.findAll {it.key != 'clazz'}.each {
                    this["${it.key}"] = it.value
                }
            }
        }
    }

    private ConfigObject loadQuartzConfig() {
        def config = ConfigurationHolder.config
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)

        // merging default Quartz config into main application config
        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DefaultQuartzConfig')))

        // merging user-defined Quartz config into main application config if provided
        try {
            config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('QuartzConfig')))
        } catch (Exception ignored) {
            // ignore, just use the defaults
        }

        return config.quartz
    }
}
