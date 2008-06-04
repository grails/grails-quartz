package org.codehaus.groovy.grails.plugins.quartz

import org.quartz.SimpleTrigger

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@prophotos.ru)
 */
class DefaultGrailsTaskClassTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader();

    void setUp() {
        ExpandoMetaClassCreationHandle.enable()
    }

    void tearDown() {
        gcl.clearCache()
    }

    void testDefaultProperties() {
        def jobClass = gcl.parseClass('class TestJob { def execute(){} }')
        def taskClass = new DefaultGrailsTaskClass(jobClass)

        assertEquals "Wrong default startDelay", 0, taskClass.startDelay
        assertEquals "Wrong default timeout", 60000, taskClass.timeout
        assertEquals "Wrong default repeatCount", SimpleTrigger.REPEAT_INDEFINITELY, taskClass.repeatCount
        assertEquals "Wrong default cronExpression", '0 0 6 * * ?', taskClass.cronExpression
        assertFalse "Task shouldn't be cron-task by default", taskClass.cronExpressionConfigured
        assertEquals "Wrong default group", 'GRAILS_JOBS', taskClass.group
        assertTrue "Task should require Hibernate session by default", taskClass.sessionRequired
        assertTrue "Task should be concurrent by default", taskClass.concurrent
    }

    void testTaskClassExecute() {
        boolean wasExecuted = false
        def testClosure = { wasExecuted = true }
        Class jobClass = gcl.parseClass("class TestJob { def testClosure; def execute() {testClosure.call()}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        taskClass.getReference().setPropertyValue("testClosure", testClosure)
        taskClass.execute()
        assertTrue "Job wasn't executed", wasExecuted
    }

    void testSimpleJob() {
        Class jobClass = gcl.parseClass("class TestJob { def timeout = 1000; def startDelay = 5000; def execute() {}}\n")
        def taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse taskClass.cronExpressionConfigured
        assertEquals 1000, taskClass.timeout
        assertEquals 5000, taskClass.startDelay

        assertEquals 1, taskClass.triggers.size()
        def trigger = taskClass.triggers['TestJob']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals SimpleTriggerFactoryBean, trigger.clazz
        assertEquals 1000, trigger.repeatInterval
        assertEquals 5000, trigger.startDelay
    }

    void testCronJob() {
        Class jobClass = gcl.parseClass("class TestJob { def cronExpression = '0 1 6 * * ?'; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertTrue taskClass.isCronExpressionConfigured()
        assertEquals '0 1 6 * * ?', taskClass.getCronExpression()

        assertEquals 1, taskClass.triggers.size()
        def trigger = taskClass.triggers['TestJob']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals CronTriggerFactoryBean, trigger.clazz
        assertEquals '0 1 6 * * ?', trigger.cronExpression
    }

    void testSessionRequiredParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def sessionRequired = false; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse "Hibernate Session shouldn't be required", taskClass.sessionRequired
    }

    void testConcurrentParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def concurrent = false; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse "Task class shouldn't be marked as concurrent", taskClass.concurrent
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def group = 'myGroup'; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertEquals 'myGroup', taskClass.group
    }

    void testWrongTimeoutOrStartDelayOrRepeatCount() {
        Class jobClass
        ['timeout', 'startDelay', 'repeatCount'].each {
            jobClass = gcl.parseClass("class TestJob { def ${it} = '1000'; def execute() {}}\n")
            shouldFail(IllegalArgumentException) {
                new DefaultGrailsTaskClass(jobClass)
            }
            // testcase for GRAILSPLUGINS-55 (integer overflow)
            jobClass = gcl.parseClass("class TestJob { def ${it} = 1000*60*60*24*30 ; def execute() {}}\n")
            shouldFail(IllegalArgumentException) {
                new DefaultGrailsTaskClass(jobClass)
            }
        }
    }

    void testWrongCronExpression() {
        def jobClass = gcl.parseClass("class TestJob { def cronExpression = 'Not a cron expression'; def execute() {}}")
        shouldFail(IllegalArgumentException) {
            new DefaultGrailsTaskClass(jobClass)
        }
    }
}