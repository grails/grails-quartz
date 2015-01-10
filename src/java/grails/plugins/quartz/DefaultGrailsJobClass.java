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

package grails.plugins.quartz;

import static grails.plugins.quartz.GrailsJobClassConstants.CONCURRENT;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_CONCURRENT;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_DESCRIPTION;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_DURABILITY;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_GROUP;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_REQUESTS_RECOVERY;
import static grails.plugins.quartz.GrailsJobClassConstants.DEFAULT_SESSION_REQUIRED;
import static grails.plugins.quartz.GrailsJobClassConstants.DESCRIPTION;
import static grails.plugins.quartz.GrailsJobClassConstants.DURABILITY;
import static grails.plugins.quartz.GrailsJobClassConstants.EXECUTE;
import static grails.plugins.quartz.GrailsJobClassConstants.GROUP;
import static grails.plugins.quartz.GrailsJobClassConstants.REQUESTS_RECOVERY;
import static grails.plugins.quartz.GrailsJobClassConstants.SESSION_REQUIRED;
import grails.plugins.quartz.config.TriggersConfigBuilder;
import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.quartz.JobExecutionContext;

/**
 * Represents a Quartz job.
 *
 * @author Micha?? K??ujszo
 * @author Marcel Overdijk
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.1
 */
public class DefaultGrailsJobClass extends AbstractInjectableGrailsClass implements GrailsJobClass {

    public static final String JOB = "Job";

    private static final Object[] NO_ARGS = {};

    private Map<String, Object> triggers = new HashMap<String, Object>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DefaultGrailsJobClass(Class<?> clazz) {
        super(clazz, JOB);

        // registering additional triggersClosure from 'triggersClosure' closure if present
        Closure triggersClosure = (Closure) GrailsClassUtils.getStaticPropertyValue(getClazz(), "triggers");
        if (triggersClosure != null) {
            triggers = new TriggersConfigBuilder(getFullName()).build(triggersClosure);
        }
    }

    public void execute() {
        getMetaClass().invokeMethod(getReferenceInstance(), EXECUTE, NO_ARGS);
    }

    public void execute(JobExecutionContext context) {
        getMetaClass().invokeMethod(getReferenceInstance(), EXECUTE, new Object[]{context});
    }

    public String getGroup() {
        return getStringValue(GROUP, DEFAULT_GROUP);
    }

    public boolean isConcurrent() {
        return getBooleanValue(CONCURRENT, DEFAULT_CONCURRENT);
    }

    public boolean isSessionRequired() {
        return getBooleanValue(SESSION_REQUIRED, DEFAULT_SESSION_REQUIRED);
    }

    public boolean isDurability() {
        return getBooleanValue(DURABILITY, DEFAULT_DURABILITY);
    }

    public boolean isRequestsRecovery() {
        return getBooleanValue(REQUESTS_RECOVERY, DEFAULT_REQUESTS_RECOVERY);
    }

    public String getDescription() {
        return getStringValue(DESCRIPTION, DEFAULT_DESCRIPTION);
    }

    public Map<String, Object> getTriggers() {
        return triggers;
    }

    protected String getStringValue(String propName, String defaultIfMissing) {
        String value = getPropertyOrStaticPropertyOrFieldValue(propName, String.class);
        return (value == null || "".equals(value)) ? defaultIfMissing : value;
    }

    protected boolean getBooleanValue(String propName, boolean defaultIfMissing) {
        Boolean value = getPropertyValue(propName, Boolean.class);
        return value == null ? defaultIfMissing : value;
   }
}
