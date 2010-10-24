import org.codehaus.groovy.grails.plugins.quartz.*
import org.codehaus.groovy.grails.plugins.quartz.listeners.*
import org.codehaus.groovy.grails.commons.spring.DefaultRuntimeSpringConfiguration
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

class QuartzPluginTests extends GroovyTestCase implements ApplicationContextAware {
    def transactional = false
    def grailsApplication
    def pluginManager
    def applicationContext

    void setUp() {
        pluginManager = PluginManagerHolder.pluginManager
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
        assertTrue "Bean 'TestJobJobClass' is not registered in application context", ctx.containsBean('TestJobJobClass')
        assertTrue "Bean 'TestJobJobDetail' is not registered in application context", ctx.containsBean('TestJobJobDetail')
        assertTrue "Bean 'TestJobTrigger' is not registered in application context", ctx.containsBean('TestJobTrigger')
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }
}
