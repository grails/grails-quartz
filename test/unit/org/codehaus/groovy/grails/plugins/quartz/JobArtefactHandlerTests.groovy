package org.codehaus.groovy.grails.plugins.quartz
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

import org.codehaus.groovy.grails.commons.ArtefactHandler

/**
 * Test case for Job artefact handler.
 *
 * @author Sergey Nebolsin
 * @since 0.2
 */
class JobArtefactHandlerTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader();

    void testJobClassWithExecuteMethod() {
        Class c = gcl.parseClass("class TestJob { def execute() { }}\n");
        ArtefactHandler handler = new JobArtefactHandler();
        assertTrue "Class *Job which defines execute() method should be recognized as a Job class", handler.isArtefact(c);
    }

    void testJobClassWithExecuteMethodWithParam() {
        Class c = gcl.parseClass("class TestJob { def execute(param) { }}\n");
        ArtefactHandler handler = new JobArtefactHandler();
        assertTrue "Class *Job which defines execute(param) method should be recognized as a Job class", handler.isArtefact(c);
    }

    void testJobClassWithWrongName() {
        Class c = gcl.parseClass("class TestController { def execute() { }}\n");
        ArtefactHandler handler = new JobArtefactHandler();
        assertFalse "Class which name doesn't end with 'Job' shouldn't be recognized as a Job class", handler.isArtefact(c);
    }

    void testJobClassWithoutExecuteMethod() {
        Class c = gcl.parseClass("class TestJob { def execute1() { }}\n");
        ArtefactHandler handler = new JobArtefactHandler();
        assertFalse "Class which doesn't declare 'execute' method shouldn't be recognized as a Job class", handler.isArtefact(c);
    }
}
