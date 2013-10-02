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

    /**
     *   If props section of config...quartz is not defined ensure that
     *   quartzProperties is not called.
     */
    void testNoQuartzPropertiesPropagation() {
        MockDoWithSpring spring = new MockDoWithSpring()
        def gcl = new GroovyClassLoader()
        def pluginDir = new File('.')
        gcl.addClasspath(pluginDir.canonicalPath)
        def pluginClass = gcl.loadClass('QuartzGrailsPlugin')
        def plugin = pluginClass.newInstance()
        def beans = plugin.doWithSpring
        beans.delegate = spring
        beans.resolveStrategy = Closure.DELEGATE_FIRST
        beans.call()
        assertTrue beans.delegate.quartzProperties == null || beans.delegate.quartzProperties.size() == 1
    }

    /**
     *
     */
    void testEmptyQuartzPropertiesPropagation() {
        MockDoWithSpring spring = new MockDoWithSpring()
        spring.application.config.quartz.getProperty('props')
        def gcl = new GroovyClassLoader()
        def pluginDir = new File('.')
        gcl.addClasspath(pluginDir.canonicalPath)
        def pluginClass = gcl.loadClass('QuartzGrailsPlugin')
        def plugin = pluginClass.newInstance()
        def beans = plugin.doWithSpring
        beans.delegate = spring
        beans.resolveStrategy = Closure.DELEGATE_FIRST
        beans.call()
        assertTrue beans.delegate.quartzProperties == [:] || beans.delegate.quartzProperties.size() == 1
    }

    /**
     *   Test that properties defined in QuartzConfig.groovy are presented to
     *   the Spring Quartz management bean.
     */
    void testQuartzConfigPropertyPropagation() {
        MockDoWithSpring spring = new MockDoWithSpring()
        def config = new ConfigObject()
        config.quartz.props.'threadPool.threadCount' = 5
        spring.application.config = config
        def gcl = new GroovyClassLoader()
        def pluginDir = new File('.')
        gcl.addClasspath(pluginDir.canonicalPath)
        def pluginClass = gcl.loadClass('QuartzGrailsPlugin')
        def plugin = pluginClass.newInstance()
        def beans = plugin.doWithSpring
        beans.delegate = spring
        beans.resolveStrategy = Closure.DELEGATE_FIRST
        beans.call()
        assertTrue beans.delegate.quartzProperties.'org.quartz.threadPool.threadCount' == '5'
    }
}
