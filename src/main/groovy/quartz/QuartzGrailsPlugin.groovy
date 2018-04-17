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
import grails.plugins.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import grails.plugins.quartz.cleanup.JdbcCleanup
import grails.plugins.quartz.listeners.ExceptionPrinterJobListener
import grails.plugins.quartz.listeners.SessionBinderJobListener
import groovy.util.logging.Slf4j
import org.grails.config.NavigableMap
import org.quartz.*
import org.quartz.impl.matchers.KeyMatcher
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.SchedulerFactoryBean

@Slf4j
class QuartzGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0.BUILD-SNAPSHOT > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

	def watchedResources = "file:./grails-app/jobs/**/*Job.groovy"


	def title = "Quartz" // Headline display name of the plugin
    def author = "Jeff Brown"
    def authorEmail = "brownj@ociweb.com"
    def description = '''\
Adds Quartz job scheduling features
'''
    def profiles = ['web']
    List loadAfter = ['hibernate3', 'hibernate4', 'hibernate5', 'services']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/quartz"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github Issues", url: "http://github.com/grails3-plugins/quartz/issues" ]


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
            Properties properties = loadQuartzProperties()


            boolean hasHibernate = hasHibernate(manager)
            def hasJdbcStore = properties['org.quartz.jdbcStore']?.toBoolean()
            if (hasJdbcStore==null) {
                hasJdbcStore = true
            }

            def pluginEnabled = properties['org.quartz.pluginEnabled']?.toBoolean()
            if (pluginEnabled==null) {
                pluginEnabled = true
            }

            if (pluginEnabled) {
                def purgeTables = properties['org.quartz.purgeQuartzTablesOnStartup']?.toBoolean()

                if (purgeTables==null) {
                    purgeTables = false
                }

                if (hasJdbcStore && hasHibernate && purgeTables) {
                    purgeTablesBean(JdbcCleanup) { bean ->
                        bean.autowire = 'byName'
                    }
                }
                // Configure job beans
                grailsApplication.jobClasses.each { GrailsJobClass jobClass ->
                    configureJobBeans.delegate = delegate
                    configureJobBeans(jobClass, hasHibernate)
                }

                // Configure the session listener if there is the Hibernate is configured
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

    /**
     * Loads the quartz stanza from the grails configuration and turns it into a
     * flattened Properties object suitable for use by the Quartz SchedulerFactoryBean.
     * @return Quartz properties as defined in the Grails Configuration object
     */
    def loadQuartzProperties() {
        Properties quartzProperties = new Properties()
        if (config.containsKey('quartz')) {
            // Convert to a properties file adding a prefix to each property
            ConfigObject configObject = new ConfigObject()
            configObject.putAll(config.quartz)
            quartzProperties << configObject.toProperties('org.quartz')
        }
        quartzProperties
    }

    def configureScheduler = { ->
        Properties properties = loadQuartzProperties()
        quartzScheduler(SchedulerFactoryBean) { bean ->
                quartzProperties = properties
                // The bean name is used by the factory bean as the scheduler name so you must explicitly set it if
                // you want a name different from the bean name.
                if (quartzProperties['org.quartz.scheduler.instanceName']) {
                    schedulerName = properties['org.quartz.scheduler.instanceName']
                }

                // delay scheduler startup to after-bootstrap stage
                if (quartzProperties['org.quartz.autoStartup']) {
                    autoStartup = false // we dont want to auto startup this bean as this bean autostartup is not grails aware.
                }
                // Store
                def hasJdbcStore = quartzProperties['org.quartz.jdbcStore']?.toBoolean()
                if (hasJdbcStore == null) {
                    hasJdbcStore = true
                }
                if (hasJdbcStore) {
                    dataSource = ref(quartzProperties['org.quartz.jdbcStoreDataSource'] ?: 'dataSource')
                    transactionManager = ref('transactionManager')
                }
                if (quartzProperties['org.quartz.waitForJobsToCompleteOnShutdown']) {
                    waitForJobsToCompleteOnShutdown = quartzProperties['org.quartz.waitForJobsToCompleteOnShutdown']?.toBoolean()
                }
                if (quartzProperties['org.quartz.exposeSchedulerInRepository']) {
                    exposeSchedulerInRepository = quartzProperties['org.quartz.exposeSchedulerInRepository']?.toBoolean()
                }


                jobFactory = quartzJobFactory

                // Global listeners on each job.
                globalJobListeners = [ref(ExceptionPrinterJobListener.NAME)]
			

        }
    }

	void onChange( Map<String, Object> event) {
		def pluginEnabled = properties['org.quartz.pluginEnabled']?.toBoolean()
		if (pluginEnabled==null) {
			pluginEnabled = true
		}

		if (pluginEnabled) {
			if(event.source) {
				boolean hasHibernate = hasHibernate(manager)
				def jobClass = grailsApplication.addArtefact(JobArtefactHandler.TYPE,event.source)
				def jobName = "${jobClass.propertyName}"
				beans {
					configureJobBeans.delegate = delegate
					configureJobBeans(jobClass, hasHibernate)
				}
			}
			refreshJobs(true)
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
                def hasJdbcStore = grailsApplication.config.getProperty('quartz.jdbcStore')?.toBoolean()
                if (hasJdbcStore == null) {
                    hasJdbcStore = true
                }

                // The session listener if is needed
                if (hasHibernate && (jobClass.sessionRequired || hasJdbcStore)) {
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
        //}
    }



	private boolean hasHibernate(manager) {
        manager?.hasGrailsPlugin("hibernate") ||
                manager?.hasGrailsPlugin("hibernate3") ||
                manager?.hasGrailsPlugin("hibernate4") ||
                manager?.hasGrailsPlugin("hibernate5")
    }

	void refreshJobs(ignoreErrors=false) {
		def pluginEnabled = grailsApplication.config.getProperty('quartz.pluginEnabled')?.toBoolean()
		if (pluginEnabled == null) {
			pluginEnabled = true
		}
		if(pluginEnabled) {
			def quartzScheduler = applicationContext.quartzScheduler

			Set<JobKey> jobKeys = applicationContext.quartzScheduler.getJobKeys(GroupMatcher.anyGroup())

			//Remove any recently removed / disabled Jobs
			jobKeys.each { JobKey key ->
				def match = grailsApplication.jobClasses.find{ GrailsJobClass jobClass -> jobClass.isEnabled() && jobClass.group == key.group && jobClass.clazz.name == key.name }
				if(!match) {
					log.info("Removing No longer Active Job: ${key.name}")
					def triggersForJob = quartzScheduler.getTriggersOfJob(key)?.collect{it.key}
					if(triggersForJob) {
						//clean up triggers before we remove the job
						quartzScheduler.unscheduleJobs(triggersForJob)
					}
					quartzScheduler.deleteJob(key)
				}
			}

			//Add new jobs
			grailsApplication.jobClasses.findAll{GrailsJobClass jobClass -> jobClass.isEnabled()}.each { GrailsJobClass jobClass ->
				try {
					scheduleJob(jobClass, applicationContext, hasHibernate(manager))
					def clz = jobClass.clazz
					clz.scheduler = applicationContext.quartzScheduler
					clz.grailsJobClass = jobClass
				} catch(ex) {
					if(ignoreErrors) {
						log.error("Error Scheduling Job Class ${jobClass} - ${ex.message}",ex)
					} else {
						throw ex
					}
				}

			}
		}

	}

	void onStartup(Map<String, Object> event) {
		def autoStart = grailsApplication.config.getProperty('quartz.autoStartup')?.toBoolean()
		if (autoStart == null) {
			autoStart = true
		}
		def pluginEnabled = grailsApplication.config.getProperty('quartz.pluginEnabled')?.toBoolean()
		if (pluginEnabled == null) {
			pluginEnabled = true
		}

		if (pluginEnabled) {
			refreshJobs()
			if(autoStart) {
				applicationContext.quartzScheduler.start()
				log.info("Quartz Scheduler - Started")
			}
		}
		log.debug("Scheduled Job Classes count: " + grailsApplication.jobClasses.size())
	}

	void onShutdown(Map<String, Object> event) {
		def waitForJobsToCompleteOnShutdown = grailsApplication.config.getProperty('quartz.waitForJobsToCompleteOnShutdown')?.toBoolean() ?: true
		applicationContext.quartzScheduler.shutdown(waitForJobsToCompleteOnShutdown)
	}
}
