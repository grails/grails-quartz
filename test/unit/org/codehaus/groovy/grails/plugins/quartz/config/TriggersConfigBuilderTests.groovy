package org.codehaus.groovy.grails.plugins.quartz.config

import org.codehaus.groovy.grails.plugins.quartz.GrailsTaskClassProperty as GTCP
import org.codehaus.groovy.grails.plugins.quartz.GrailsSimpleTriggerBean
import org.codehaus.groovy.grails.plugins.quartz.GrailsCronTriggerBean

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@prophotos.ru)
 */
class TriggersConfigBuilderTests extends GroovyTestCase {
    void testConfigBuilder() {
        def builder = new TriggersConfigBuilder('TestJob')
        def closure = {
            simpleTrigger()
            simpleTrigger timeout:1000
            simpleTrigger startDelay:500
            simpleTrigger startDelay:500, timeout: 1000
            simpleTrigger startDelay:500, timeout: 1000, repeatCount: 3
            simpleTrigger 'everySecond', timeout:1000
            cronTrigger()
            cronTrigger cronExpression:'0 15 6 * * ?'
            cronTrigger 'myTrigger', cronExpression:'0 15 6 * * ?'
        }
        builder.build(closure)

        assertEquals 'Invalid triggers count', 9, builder.triggers.size()

        assertPropertiesEquals(new Expando(
                name:'TestJob0',
                clazz:GrailsSimpleTriggerBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob0']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob1',
                clazz:GrailsSimpleTriggerBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob1']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob2',
                clazz:GrailsSimpleTriggerBean,
                startDelay: 500,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob2']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob3',
                clazz:GrailsSimpleTriggerBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob3']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob4',
                clazz:GrailsSimpleTriggerBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 3
            ), builder.triggers['TestJob4']
        )

        assertPropertiesEquals(new Expando(
                name:'TestJobEverySecond',
                clazz:GrailsSimpleTriggerBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJobEverySecond']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob5',
                clazz:GrailsCronTriggerBean,
                startDelay:GTCP.DEFAULT_START_DELAY,
                cronExpression: GTCP.DEFAULT_CRON_EXPRESSION
            ), builder.triggers['TestJob5']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob6',
                clazz:GrailsCronTriggerBean,
                cronExpression: '0 15 6 * * ?',
                startDelay: GTCP.DEFAULT_START_DELAY
            ), builder.triggers['TestJob6']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJobMyTrigger',
                clazz:GrailsCronTriggerBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                cronExpression: '0 15 6 * * ?'
            ), builder.triggers['TestJobMyTrigger']
        )

    }

    private assertPropertiesEquals(expected, actual) {
        expected.properties.each { entry ->
            assertEquals "Unexpected value for property: ${entry.key}", entry.value, actual[entry.key]
        }
        assertEquals 'Different number of properties', expected.properties.size(), actual.properties.size()
    }

}