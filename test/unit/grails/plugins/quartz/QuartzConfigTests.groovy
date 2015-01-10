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

import grails.test.GrailsUnitTestCase

class QuartzConfigTests extends GrailsUnitTestCase {

    private MockDoWithSpring spring = new MockDoWithSpring()

    /**
     * If props section of config...quartz is not defined ensure that quartzProperties is not called.
     */
    void testNoQuartzPropertiesPropagation() {

        doWithSpring()

        assert spring.quartzProperties == null || spring.quartzProperties.size() == 1
    }

    void testEmptyQuartzPropertiesPropagation() {
        spring.application.config.quartz.getProperty('props')

        doWithSpring()

        assert spring.quartzProperties == [:] || spring.quartzProperties.size() == 1
    }

    /**
     * Test that properties defined in QuartzConfig.groovy are presented to the Spring Quartz management bean.
     */
    void testQuartzConfigPropertyPropagation() {
        def config = new ConfigObject()
        config.quartz.props.'threadPool.threadCount' = 5
        spring.application.config = config

        doWithSpring()

        assert spring.quartzProperties.'org.quartz.threadPool.threadCount' == '5'
    }

    protected newPluginInstance() {
        def gcl = new GroovyClassLoader()
        gcl.addClasspath(new File('.').canonicalPath)
        def pluginClass = gcl.loadClass('QuartzGrailsPlugin')
        pluginClass.newInstance()
    }

    protected void doWithSpring() {
        def beans = newPluginInstance().doWithSpring
        beans.delegate = spring
        beans.resolveStrategy = Closure.DELEGATE_FIRST
        beans()
    }
}
