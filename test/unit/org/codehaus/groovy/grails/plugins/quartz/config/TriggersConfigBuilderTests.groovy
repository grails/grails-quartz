package org.codehaus.groovy.grails.plugins.quartz.config

import org.codehaus.groovy.grails.plugins.quartz.GrailsTaskClassProperty as GTCP
import org.codehaus.groovy.grails.plugins.quartz.SimpleTriggerFactoryBean
import org.codehaus.groovy.grails.plugins.quartz.CronTriggerFactoryBean

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
            simpleTrigger startDelay:500, timeout: 1000, repeatCount: 0
        }
        builder.build(closure)

        assertEquals 'Invalid triggers count', 10, builder.triggers.size()

        assertPropertiesEquals(new Expando(
                name:'TestJob0',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob0']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob1',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob1']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob2',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob2']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob3',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob3']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob4',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 3
            ), builder.triggers['TestJob4']
        )

        assertPropertiesEquals(new Expando(
                name:'TestJobEverySecond',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJobEverySecond']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob5',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CronTriggerFactoryBean,
                startDelay:GTCP.DEFAULT_START_DELAY,
                cronExpression: GTCP.DEFAULT_CRON_EXPRESSION
            ), builder.triggers['TestJob5']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob6',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CronTriggerFactoryBean,
                cronExpression: '0 15 6 * * ?',
                startDelay: GTCP.DEFAULT_START_DELAY
            ), builder.triggers['TestJob6']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJobMyTrigger',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CronTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                cronExpression: '0 15 6 * * ?'
            ), builder.triggers['TestJobMyTrigger']
        )

        assertPropertiesEquals(new Expando(
                name:'TestJob7',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:SimpleTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 0
            ), builder.triggers['TestJob7']
        )
    }

    private assertPropertiesEquals(expected, actual) {
        expected.properties.each { entry ->
            assertEquals "Unexpected value for property: ${entry.key}", entry.value, actual[entry.key]
        }
        assertEquals 'Different number of properties', expected.properties.size(), actual.properties.size()
    }

}