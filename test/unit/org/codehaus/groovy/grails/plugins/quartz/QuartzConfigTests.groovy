package org.codehaus.groovy.grails.plugins.quartz

import grails.test.GrailsUnitTestCase
import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.util.GrailsUtil

class QuartzConfigTests extends GrailsUnitTestCase {

    /**
    *   If props section of config...quartz is not defined ensure that 
    *   quartzProperties is not called.
    */
    void testNoQuartzPropertiesPropagation() {
       ConfigurationHolder.config = new ConfigObject()
       def gcl = new GroovyClassLoader()
       def pluginDir = new File('.')
       gcl.addClasspath( pluginDir.canonicalPath)
       def pluginClass = gcl.loadClass( 'QuartzGrailsPlugin')
       def plugin = pluginClass.newInstance()
       def beans = plugin.doWithSpring
       beans.delegate = new MockDoWithSpring()
       beans.resolveStrategy = Closure.DELEGATE_FIRST
       beans.call()
       assertNull beans.delegate.quartzProperties
       }
   
    /**
    *   
    */
    void testEmptyQuartzPropertiesPropagation() {
        ConfigurationHolder.config = new ConfigObject()
        ConfigurationHolder.config.quartz.getProperty( 'props')
        def gcl = new GroovyClassLoader()
        def pluginDir = new File('.')
        gcl.addClasspath( pluginDir.canonicalPath)
        def pluginClass = gcl.loadClass( 'QuartzGrailsPlugin')
        def plugin = pluginClass.newInstance()
        def beans = plugin.doWithSpring
        beans.delegate = new MockDoWithSpring()
        beans.resolveStrategy = Closure.DELEGATE_FIRST
        beans.call()
        assertTrue beans.delegate.quartzProperties == [:]
    }

    /**
    *   Test that properties defined in QuartzConfig.groovy are presented to 
    *   the Spring Quartz management bean.
    */
    void testQuartzConfigPropertyPropagation() {
       def config = new ConfigObject()
       config.quartz.props.'threadPool.threadCount' = 5
       ConfigurationHolder.config = config
       def gcl = new GroovyClassLoader()
       def pluginDir = new File('.')
       gcl.addClasspath( pluginDir.canonicalPath)
       def pluginClass = gcl.loadClass( 'QuartzGrailsPlugin')
       def plugin = pluginClass.newInstance()
       def beans = plugin.doWithSpring
       beans.delegate = new MockDoWithSpring()
       beans.resolveStrategy = Closure.DELEGATE_FIRST
       beans.call()
       assertTrue beans.delegate.quartzProperties.'org.quartz.threadPool.threadCount' == '5'
   }
}