package org.codehaus.groovy.grails.plugins.quartz

import org.quartz.SimpleTrigger

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 */
class DefaultGrailsJobClassTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader();

    void setUp() {
        ExpandoMetaClassCreationHandle.enable()
    }

    void tearDown() {
        gcl.clearCache()
    }

    void testDefaultProperties() {
        def jobClass = gcl.parseClass('class TestJob { def execute(){} }')
        def grailsJobClass = new DefaultGrailsJobClass(jobClass)

        assertEquals "Wrong default startDelay", 0, grailsJobClass.startDelay
        assertEquals "Wrong default timeout", 60000, grailsJobClass.timeout
        assertEquals "Wrong default repeatCount", SimpleTrigger.REPEAT_INDEFINITELY, grailsJobClass.repeatCount
        assertEquals "Wrong default cronExpression", '0 0 6 * * ?', grailsJobClass.cronExpression
        assertFalse "Job shouldn't be cron-job by default", grailsJobClass.cronExpressionConfigured
        assertEquals "Wrong default group", 'GRAILS_JOBS', grailsJobClass.group
        assertTrue "Job should require Hibernate session by default", grailsJobClass.sessionRequired
        assertTrue "Job should be concurrent by default", grailsJobClass.concurrent
    }

    void testJobClassExecute() {
        boolean wasExecuted = false
        def testClosure = { wasExecuted = true }
        Class jobClass = gcl.parseClass("class TestJob { def testClosure; def execute() {testClosure.call()}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        grailsJobClass.getReference().setPropertyValue("testClosure", testClosure)
        grailsJobClass.execute()
        assertTrue "Job wasn't executed", wasExecuted
    }

    void testSimpleJob() {
        Class jobClass = gcl.parseClass("class TestJob { def timeout = 1000; def startDelay = 5000; def execute() {}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse grailsJobClass.cronExpressionConfigured
        assertEquals 1000, grailsJobClass.timeout
        assertEquals 5000, grailsJobClass.startDelay

        assertEquals 1, grailsJobClass.triggers.size()
        def trigger = grailsJobClass.triggers['TestJob']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals CustomTriggerFactoryBean, trigger.clazz
        assertEquals 1000, trigger.repeatInterval
        assertEquals 5000, trigger.startDelay
    }

    void testCronJob() {
        Class jobClass = gcl.parseClass("class TestJob { def cronExpression = '0 1 6 * * ?'; def execute() {}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertTrue grailsJobClass.isCronExpressionConfigured()
        assertEquals '0 1 6 * * ?', grailsJobClass.getCronExpression()

        assertEquals 1, grailsJobClass.triggers.size()
        def trigger = grailsJobClass.triggers['TestJob']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals CronTriggerFactoryBean, trigger.clazz
        assertEquals '0 1 6 * * ?', trigger.cronExpression
    }

    void testSessionRequiredParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def sessionRequired = false; def execute() {}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Hibernate Session shouldn't be required", grailsJobClass.sessionRequired
    }

    void testConcurrentParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def concurrent = false; def execute() {}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Job class shouldn't be marked as concurrent", grailsJobClass.concurrent
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def group = 'myGroup'; def execute() {}}\n")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals 'myGroup', grailsJobClass.group
    }

    void testWrongTimeoutOrStartDelayOrRepeatCount() {
        Class jobClass
        ['timeout', 'startDelay', 'repeatCount'].each {
            jobClass = gcl.parseClass("class TestJob { def ${it} = '1000'; def execute() {}}\n")
            shouldFail(IllegalArgumentException) {
                new DefaultGrailsJobClass(jobClass)
            }
            // testcase for GRAILSPLUGINS-55 (integer overflow)
            jobClass = gcl.parseClass("class TestJob { def ${it} = 1000*60*60*24*30 ; def execute() {}}\n")
            shouldFail(IllegalArgumentException) {
                new DefaultGrailsJobClass(jobClass)
            }
        }
    }

    void testWrongCronExpression() {
        def jobClass = gcl.parseClass("class TestJob { def cronExpression = 'Not a cron expression'; def execute() {}}")
        shouldFail(IllegalArgumentException) {
            new DefaultGrailsJobClass(jobClass)
        }
    }
}