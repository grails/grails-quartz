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

import grails.plugins.quartz.JobArtefactHandler
import grails.plugins.quartz.listeners.ExceptionPrinterJobListener

import org.codehaus.groovy.grails.commons.spring.DefaultRuntimeSpringConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class QuartzPluginTests extends GroovyTestCase implements ApplicationContextAware {

    static transactional = false

    def grailsApplication
    def pluginManager
    ApplicationContext applicationContext

    void testLoading() {
        assert pluginManager, 'Plugin manager is null'
        assert pluginManager.hasGrailsPlugin('core'), 'Core plugin is not loaded'
//        assert pluginManager.hasGrailsPlugin('hibernate'), 'Hibernate plugin is not loaded'
        assert pluginManager.hasGrailsPlugin('quartz'), 'Quartz plugin is not loaded'

        assert applicationContext.containsBean('quartzScheduler'), "Bean 'quartzScheduler' is not registered in application context"
//        assert applicationContext.containsBean(SessionBinderJobListener.NAME), "Bean '$SessionBinderJobListener.NAME' is not registered in application context"
        assert applicationContext.containsBean(ExceptionPrinterJobListener.NAME), "Bean '$ExceptionPrinterJobListener.NAME' is not registered in application context"
    }

    void testArtefactHandlerRegistering() {
        def handler = grailsApplication.artefactHandlers.find { it.type == 'Job' }
        assert handler, 'Job artefact handler was not registered'
        assert handler instanceof JobArtefactHandler, 'Job artefact handler should be of type JobArtefactHandler'
    }

    void testJobRegistering() {
        Class jobClass = grailsApplication.classLoader.parseClass("""
            class TestJob {
                def timeout = 1000
                def startDelay = 5000
                void execute() {}
            }""")
        assert grailsApplication.isArtefactOfType(JobArtefactHandler.TYPE, jobClass)

        grailsApplication.addArtefact( JobArtefactHandler.TYPE, jobClass)
        def plugin = pluginManager.getGrailsPlugin("quartz")
        def springConfig = new DefaultRuntimeSpringConfiguration(grailsApplication.parentContext)
        plugin.doWithRuntimeConfiguration(springConfig)

        assert springConfig.applicationContext.containsBean('TestJob'), "Bean 'TestJob' is not registered in application context"
    }
}
