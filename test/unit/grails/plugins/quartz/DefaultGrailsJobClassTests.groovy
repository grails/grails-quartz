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

package grails.plugins.quartz

class DefaultGrailsJobClassTests extends GroovyTestCase {

    protected GroovyClassLoader gcl = new GroovyClassLoader()

    protected void tearDown() {
        super.tearDown()
        gcl.clearCache()
    }

    void testDefaultProperties() {
        def jobClass = gcl.parseClass('class TestJob { void execute() {} }')
        def grailsJobClass = new DefaultGrailsJobClass(jobClass)

        assert 'GRAILS_JOBS' == grailsJobClass.group, 'Wrong default group'
        assert grailsJobClass.sessionRequired, 'Job should require Hibernate session by default'
        assert grailsJobClass.concurrent, 'Job should be concurrent by default'
    }

    void testJobClassExecute() {
        boolean wasExecuted = false
        def testClosure = { wasExecuted = true }
        Class jobClass = gcl.parseClass("""
            class TestJob {
                def testClosure
                void execute() {
                    testClosure.call()
                }
            }""")
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        grailsJobClass.referenceInstance.testClosure = testClosure
        grailsJobClass.execute()
        assert wasExecuted, "Job wasn't executed"
    }

    void testSessionRequiredParameter() {
        Class jobClass = gcl.parseClass("""
            class TestJob {
                boolean sessionRequired = false
                void execute() {}
            }
            """)
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assert !grailsJobClass.sessionRequired, "Hibernate Session shouldn't be required"
    }

    void testConcurrentParameter() {
        Class jobClass = gcl.parseClass("""
            class TestJob {
                boolean concurrent = false
                void execute() {}
            }
            """)
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assert !grailsJobClass.concurrent, "Job class shouldn't be marked as concurrent"
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("""
            class TestJob {
                String group = 'myGroup'
                void execute() {}
            }
            """)
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assert 'myGroup' == grailsJobClass.group
    }
}
