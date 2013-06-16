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

import org.codehaus.groovy.grails.commons.spring.DefaultRuntimeSpringConfiguration
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import grails.plugins.quartz.JobArtefactHandler
import grails.plugins.quartz.listeners.ExceptionPrinterJobListener
import grails.plugins.quartz.listeners.SessionBinderJobListener

class QuartzPluginTests extends GroovyTestCase implements ApplicationContextAware {
    def transactional = false
    def grailsApplication
    def pluginManager
    def applicationContext

    void setUp() {
    }

    void testLoading() {
        assertNotNull 'Plugin manager is null', pluginManager
        assertTrue 'Core plugin is not loaded', pluginManager.hasGrailsPlugin('core')
        assertTrue 'Hibernate plugin is not loaded', pluginManager.hasGrailsPlugin('hibernate')
        assertTrue 'Quartz plugin is not loaded', pluginManager.hasGrailsPlugin('quartz')

        assertTrue "Bean 'quartzScheduler' is not registered in application context", applicationContext.containsBean('quartzScheduler')
        assertTrue "Bean '${SessionBinderJobListener.NAME}' is not registered in application context", applicationContext.containsBean("${SessionBinderJobListener.NAME}")
        assertTrue "Bean '${ExceptionPrinterJobListener.NAME}' is not registered in application context", applicationContext.containsBean("${ExceptionPrinterJobListener.NAME}")
    }

    void testArtefactHandlerRegistering() {
        def handler = grailsApplication.artefactHandlers.find { it.type == 'Job' }
        assertNotNull "Job artefact handler was not registered", handler
        assertTrue "Job artefact handler should be of type JobArtefactHandler", handler instanceof JobArtefactHandler
    }

    void testJobRegistering() {
        Class jobClass = grailsApplication.classLoader.parseClass("class TestJob { def timeout = 1000; def startDelay = 5000; def execute() {}}\n")
        assertTrue grailsApplication.isArtefactOfType( JobArtefactHandler.TYPE, jobClass )
        grailsApplication.addArtefact( JobArtefactHandler.TYPE, jobClass )
        def plugin = pluginManager.getGrailsPlugin("quartz")
        def springConfig = new DefaultRuntimeSpringConfiguration(grailsApplication.parentContext)
        plugin.doWithRuntimeConfiguration(springConfig)

        def ctx = springConfig.applicationContext
        assertTrue "Bean 'TestJob' is not registered in application context", ctx.containsBean('TestJob')
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }
}
