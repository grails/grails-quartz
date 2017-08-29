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
        def jobClass = gcl.parseClass('class TestJob { def execute(){} }')
        def grailsJobClass = new DefaultGrailsJobClass(jobClass)

        assertEquals "Wrong default group", 'GRAILS_JOBS', grailsJobClass.group
        assertTrue "Job should require Hibernate session by default", grailsJobClass.sessionRequired
        assertTrue "Job should be concurrent by default", grailsJobClass.concurrent
    }

    void testJobClassExecute() {
        boolean wasExecuted = false
        def testClosure = { wasExecuted = true }
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    static testClosure
                    def execute() {
                        testClosure.call()
                    }
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        grailsJobClass.referenceInstance.testClosure = testClosure
        grailsJobClass.execute()
        assertTrue "Job wasn't executed", wasExecuted
    }

    void testSessionRequiredParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    static sessionRequired = false
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Hibernate Session shouldn't be required", grailsJobClass.sessionRequired
    }

    void testConcurrentParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    static concurrent = false
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Job class shouldn't be marked as concurrent", grailsJobClass.concurrent
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    static group = 'myGroup'
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals 'myGroup', grailsJobClass.group
    }
}
