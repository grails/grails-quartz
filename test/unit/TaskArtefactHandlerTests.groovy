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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.grails.commons.ArtefactHandler
import org.codehaus.groovy.grails.plugins.quartz.TaskArtefactHandler;

/**
 * Test case for Task artefact handler.
 *
 * @author Sergey Nebolsin
 * @since 0.2
 */
class TaskArtefactHandlerTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader();

    void testIsTaskClass() {
        Class c = gcl.parseClass("class TestJob { def execute() { }}\n");
        ArtefactHandler handler = new TaskArtefactHandler();
        assertTrue handler.isArtefact(c);
    }

    void testIsNotATaskClass() {
        // wrong method name
        Class c = gcl.parseClass("class TestJob { def execute1() { }}\n");
        ArtefactHandler handler = new TaskArtefactHandler();
        assertFalse handler.isArtefact(c);
        // wrong class name
        c = gcl.parseClass("class TestController { def execute() { }}\n");
        assertFalse handler.isArtefact(c);
    }
}
