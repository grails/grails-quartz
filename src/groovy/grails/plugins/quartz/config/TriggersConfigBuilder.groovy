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

package grails.plugins.quartz.config

import grails.plugins.quartz.CustomTriggerFactoryBean
import grails.plugins.quartz.GrailsJobClassConstants as Constants
import grails.util.GrailsUtil

import org.quartz.CronExpression
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.util.Assert

/**
 * Parses trigger configuration info.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 *
 * @since 0.3
 */
class TriggersConfigBuilder extends BuilderSupport {
    private int triggerNumber = 0
    private String jobName

    Map triggers = [:]

    TriggersConfigBuilder(String name) {
        jobName = name
    }

    /**
     * Evaluate triggers closure.
     */
    Map build(Closure closure) {
        closure.delegate = this
        closure()
        return triggers
    }

    /**
     * Create a trigger.
     *
     * @param name the name of the method to create trigger. It's trigger type: simple, cron, custom.
     * @param attributes trigger attributes
     * @return trigger definitions
     */
    Expando createTrigger(String name, Map attributes) {

        Map triggerAttributes = attributes ? new HashMap(attributes) : [:]

        prepareCommonTriggerAttributes(triggerAttributes)

        Class<?> triggerClass
        switch (normalizeTriggerType(name)) {
            case 'simple':
                triggerClass = SimpleTriggerImpl
                prepareSimpleTriggerAttributes(triggerAttributes)
                break
            case 'cron':
                triggerClass = CronTriggerImpl
                prepareCronTriggerAttributes(triggerAttributes)
                break
            case 'custom':
                triggerClass = triggerAttributes.remove('triggerClass')
                Assert.notNull triggerClass, "Custom trigger must have 'triggerClass' attribute"
                     Assert.isAssignable Trigger, triggerClass, "Custom trigger class must implement org.quartz.Trigger class."
                break
            default:
                throw new Exception("Invalid format")
        }

        new Expando(clazz: CustomTriggerFactoryBean, triggerClass: triggerClass, triggerAttributes: triggerAttributes)
    }

    /**
     * Convert old trigger types' names
     *
     * @param old or new trigger type
     * @return new trigger type
     */
    private String normalizeTriggerType(name) {

        def warn = { String oldName, String newName ->
            GrailsUtil.deprecated("You're using deprecated '$oldName' construction in the $jobName, use '$newName' instead.")
            return newName
        }

        switch (name) {
            case 'simpleTrigger': return warn('simpleTrigger', 'simple')
            case 'cronTrigger':   return warn('cronTrigger',   'cron')
            case 'customTrigger': return warn('customTrigger', 'custom')
            default:              return name
        }
    }

    private prepareCommonTriggerAttributes(Map triggerAttributes) {
        def prepare = prepareTriggerAttribute.curry(triggerAttributes)

        if (triggerAttributes[Constants.NAME] == null) {
            triggerAttributes[Constants.NAME] = "$jobName${triggerNumber++}".toString()
        }

        prepare Constants.GROUP, Constants.DEFAULT_TRIGGERS_GROUP.toString()
        prepare Constants.START_DELAY, Constants.DEFAULT_START_DELAY, {
            assertNotNegativeAndIntegerOrLong it, 'startDelay'
        }
    }

    private prepareSimpleTriggerAttributes(Map triggerAttributes) {
        def prepare = prepareTriggerAttribute.curry(triggerAttributes)

        // Process the old deprecated property "timeout"
        def timeout = triggerAttributes.remove(Constants.TIMEOUT)
        if (timeout != null) {
            GrailsUtil.deprecated("You're using deprecated 'timeout' property in the $jobName, use 'repeatInterval' instead")

            assertNotNegativeAndIntegerOrLong timeout, 'timeout'

            triggerAttributes[Constants.REPEAT_INTERVAL] = timeout
        }

        // Validate repeat interval
        prepare Constants.REPEAT_INTERVAL, Constants.DEFAULT_REPEAT_INTERVAL, {
            assertNotNegativeAndIntegerOrLong it, 'repeatInterval'
        }

        // Validate repeat count
        prepare Constants.REPEAT_COUNT, Constants.DEFAULT_REPEAT_COUNT, {
            assertIntegerOrLong it, 'repeatCount'
            Assert.isTrue it.longValue() >= 0 || it.longValue() == SimpleTrigger.REPEAT_INDEFINITELY,
                    "repeatCount trigger property for job class $jobName is negative (possibly integer overflow error)"
        }
    }

    private prepareCronTriggerAttributes(Map triggerAttributes) {
        prepareTriggerAttribute triggerAttributes, Constants.CRON_EXPRESSION, Constants.DEFAULT_CRON_EXPRESSION, {
            Assert.isTrue CronExpression.isValidExpression(it.toString()),
                    "Cron expression '$it' in the job class $jobName is not a valid cron expression"
        }
    }

    private prepareTriggerAttribute = { Map attributes, String name, defaultValue, validate = {} ->
        if (attributes[name] == null) {
            attributes[name] = defaultValue
        }
        validate(attributes[name])
    }

    /**
     * Does nothing. Implements the BuilderSupport method.
     */
    protected void setParent(parent, child) {
        // Nothing!
    }

    /**
     * Implements the BuilderSupport method.
     */
    protected createNode(name) {
        createNode(name, null, null)
    }

    /**
     * Implements the BuilderSupport method.
     */
    protected createNode(name, value) {
        createNode(name, null, value)
    }

    /**
     * Implements the BuilderSupport method.
     */
    protected createNode(name, Map attributes) {
        createNode(name, attributes, null)
    }

    /**
     * Create a trigger. Implements the BuilderSupport method.
     */
    protected createNode(name, Map attributes, value) {
        def trigger = createTrigger(name, attributes)
        triggers[trigger.triggerAttributes.name.toString()] = trigger
        trigger
    }

    protected void assertIntegerOrLong(value, String propertyName) {
        Assert.isTrue(
              (value instanceof Integer || value instanceof Long),
              "$propertyName trigger property in the job class $jobName must be Integer or Long")
    }

    protected void assertNotNegative(Number value, String propertyName) {
         Assert.isTrue(
             value.longValue() >= 0,
             "$propertyName trigger property for job class $jobName is negative (possibly integer overflow error)")
    }

    protected void assertNotNegativeAndIntegerOrLong(value, String propertyName) {
        assertIntegerOrLong value, propertyName
        assertNotNegative value, propertyName
    }
}
