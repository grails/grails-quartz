/* Copyright 2004-2005 the original author or authors.
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
import org.codehaus.groovy.grails.plugins.quartz.GrailsTaskClass
import org.codehaus.groovy.grails.plugins.quartz.DefaultGrailsTaskClass

/**
 * Test case for default GrailsTaskClass implementation.
 *
 * @author Sergey Nebolsin
 * @since 0.2
 */
class GrailsTaskClassTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader();

    void tearDown() {
        gcl.clearCache()
    }

    void testSimpleJob() {
		Class jobClass = gcl.parseClass("class TestJob { def timeout = 1000; def startDelay = 5000; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse taskClass.isCronExpressionConfigured()
        assertEquals 1000, taskClass.getTimeout()
        assertEquals 5000, taskClass.getStartDelay()
    }

    void testWrongTimeoutOrStartDelay() {
        Class jobClass
        ['timeout','startDelay'].each {
            jobClass = gcl.parseClass("class TestJob { def ${it} = '1000'; def execute() {}}\n")
            shouldFail( IllegalArgumentException.class ) {
                new DefaultGrailsTaskClass(jobClass)
            }
            // testcase for GRAILSPLUGINS-55 (integer overflow)
            jobClass = gcl.parseClass("class TestJob { def ${it} = 1000*60*60*24*30 ; def execute() {}}\n")
            shouldFail( IllegalArgumentException.class ) {
                new DefaultGrailsTaskClass(jobClass)
            }
        }
    }

    void testCronJob() {
		Class jobClass = gcl.parseClass("class TestJob { def cronExpression = '0 0 6 * * ?'; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertTrue taskClass.isCronExpressionConfigured()
        assertEquals '0 0 6 * * ?', taskClass.getCronExpression()
    }

    void testSessionRequiredParameter() {
		Class jobClass = gcl.parseClass("class TestJob { def sessionRequired = false; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse "Hibernate Session shouldn't be required", taskClass.isSessionRequired()
    }

    void testConcurrentParameter() {
		Class jobClass = gcl.parseClass("class TestJob { def concurrent = false; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertFalse "Task class shouldn't be marked as concurrent", taskClass.isConcurrent()
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("class TestJob { def group = 'myGroup'; def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertEquals 'myGroup', taskClass.getGroup()
    }

    void testDefaultJob() {
        Class jobClass = gcl.parseClass("class TestJob { def execute() {}}\n")
        GrailsTaskClass taskClass = new DefaultGrailsTaskClass(jobClass)
        assertEquals 'Wrong name for default task class', 'Test', taskClass.getName()
        assertEquals 'Wrong full name for default task class', 'TestJob', taskClass.getFullName()
        assertEquals 'Jobs should be placed in group GRAILS_JOBS by default', 'GRAILS_JOBS', taskClass.getGroup()
        assertFalse 'Default task class type should be Simple, not Cron', taskClass.isCronExpressionConfigured()
        assertEquals 'Default tast class timeout should be 1 minute', 60000, taskClass.getTimeout()
        assertEquals 'Default tast class startDelay should be 0', 0, taskClass.getStartDelay()
        assertTrue 'Default task class should be concurrent', taskClass.isConcurrent()
        assertTrue 'Default task class should require Hibernate session', taskClass.isSessionRequired()
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
}
