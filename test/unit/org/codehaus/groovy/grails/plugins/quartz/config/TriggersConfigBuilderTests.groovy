package org.codehaus.groovy.grails.plugins.quartz.config

import org.codehaus.groovy.grails.plugins.quartz.GrailsTaskClassProperty as GTCP
import org.codehaus.groovy.grails.plugins.quartz.CustomTriggerFactoryBean

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@prophotos.ru)
 */
class TriggersConfigBuilderTests extends GroovyTestCase {
    void testConfigBuilder() {
        def builder = new TriggersConfigBuilder('TestJob')
        def closure = {
            simple()
            simple timeout:1000
            simple startDelay:500
            simple startDelay:500, timeout: 1000
            simple startDelay:500, timeout: 1000, repeatCount: 3
            simple 'everySecond', timeout:1000
            cron()
            cron cronExpression:'0 15 6 * * ?'
            cron 'myTrigger', cronExpression:'0 15 6 * * ?'
            simple startDelay:500, timeout: 1000, repeatCount: 0
        }
        builder.build(closure)

        assertEquals 'Invalid triggers count', 10, builder.triggers.size()

        assertPropertiesEquals(new Expando(
                name:'TestJob0',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob0']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob1',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob1']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob2',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: GTCP.DEFAULT_TIMEOUT,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob2']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob3',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJob3']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob4',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 3
            ), builder.triggers['TestJob4']
        )

        assertPropertiesEquals(new Expando(
                name:'TestJobEverySecond',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: GTCP.DEFAULT_REPEAT_COUNT
            ), builder.triggers['TestJobEverySecond']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob5',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay:GTCP.DEFAULT_START_DELAY,
                cronExpression: GTCP.DEFAULT_CRON_EXPRESSION
            ), builder.triggers['TestJob5']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJob6',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                cronExpression: '0 15 6 * * ?',
                startDelay: GTCP.DEFAULT_START_DELAY
            ), builder.triggers['TestJob6']
        )
        assertPropertiesEquals(new Expando(
                name:'TestJobMyTrigger',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
                startDelay: GTCP.DEFAULT_START_DELAY,
                cronExpression: '0 15 6 * * ?'
            ), builder.triggers['TestJobMyTrigger']
        )

        assertPropertiesEquals(new Expando(
                name:'TestJob7',
                group: GTCP.DEFAULT_TRIGGERS_GROUP,
                clazz:CustomTriggerFactoryBean,
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