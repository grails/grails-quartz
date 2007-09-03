import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.plugins.DefaultGrailsPlugin
import org.codehaus.groovy.grails.plugins.quartz.*
import org.codehaus.groovy.grails.plugins.quartz.listeners.*
import org.codehaus.groovy.grails.commons.spring.DefaultRuntimeSpringConfiguration

class QuartzPluginTests extends GroovyTestCase {
    def grailsApplication
    def pluginManager

    void testLoading() {
        assertNotNull 'Plugin manager is null', pluginManager
        assertTrue 'Core plugin is not loaded', pluginManager.hasGrailsPlugin('core')
        assertTrue 'Hibernate plugin is not loaded', pluginManager.hasGrailsPlugin('hibernate')
        assertTrue 'Quartz plugin is not loaded', pluginManager.hasGrailsPlugin('quartz')

        def ctx = grailsApplication.parentContext
        assertTrue ctx.containsBean('quartzScheduler')
        assertTrue ctx.containsBean("${SessionBinderJobListener.NAME}")
        assertTrue ctx.containsBean("${ExceptionPrinterJobListener.NAME}")
        assertTrue ctx.containsBean("${ExecutionControlTriggerListener.NAME}")
    }

    void testArtefactHandlerRegistering() {
        def handler = grailsApplication.artefactHandlers.find { it.type == 'Task' }
        assertNotNull "Task artefact handler was not registered", handler
        assertTrue "Task artefact handler should be of type TaskArtefactHandler", handler instanceof TaskArtefactHandler
    }

    void testJobRegistering() {
        Class jobClass = grailsApplication.classLoader.parseClass("class TestJob { def timeout = 1000; def startDelay = 5000; def execute() {}}\n")
        assertTrue grailsApplication.isArtefactOfType( TaskArtefactHandler.TYPE, jobClass )
        grailsApplication.addArtefact( TaskArtefactHandler.TYPE, jobClass )
        def plugin = pluginManager.getGrailsPlugin("quartz")
        def springConfig = new DefaultRuntimeSpringConfiguration(grailsApplication.parentContext)
        plugin.doWithRuntimeConfiguration(springConfig)

        def ctx = springConfig.applicationContext
        assertTrue ctx.containsBean('TestJob')
        assertTrue ctx.containsBean('TestJobJobClass')
        assertTrue ctx.containsBean('TestJobJobDetail')
        assertTrue ctx.containsBean('TestJobTrigger')
    }
}
