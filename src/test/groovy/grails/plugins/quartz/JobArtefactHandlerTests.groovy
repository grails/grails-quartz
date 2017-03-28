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

import grails.core.ArtefactHandler

/**
 * Test case for Job artefact handler.
 *
 * @author Sergey Nebolsin
 * @since 0.2
 */
class JobArtefactHandlerTests extends GroovyTestCase {
    private ArtefactHandler handler = new JobArtefactHandler()
    protected GroovyClassLoader gcl = new GroovyClassLoader()

    void testJobClassWithExecuteMethod() {
        Class c = gcl.parseClass("class TestJob { def execute() { }}\n")
        assertTrue "Class *Job which defines execute() method should be recognized as a Job class", handler.isArtefact(c)
    }

    void testJobClassWithExecuteMethodWithParam() {
        Class c = gcl.parseClass("class TestJob { def execute(param) { }}\n")
        assertTrue "Class *Job which defines execute(param) method should be recognized as a Job class", handler.isArtefact(c)
    }

    void testJobClassWithWrongName() {
        Class c = gcl.parseClass("class TestController { def execute() { }}\n")
        assertFalse "Class which name doesn't end with 'Job' shouldn't be recognized as a Job class", handler.isArtefact(c)
    }

    void testJobClassWithoutExecuteMethod() {
        Class c = gcl.parseClass("class TestJob { def execute1() { }}\n")
        assertFalse "Class which doesn't declare 'execute' method shouldn't be recognized as a Job class", handler.isArtefact(c)
    }
}
