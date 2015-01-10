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

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.triggers.AbstractTrigger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates and registers trigger beans in the Spring context.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 */
public class CustomTriggerFactoryBean implements FactoryBean<Trigger>, InitializingBean {
    private Class<Trigger> triggerClass;
    private Trigger customTrigger;
    private JobDetail jobDetail;

    private Map<?, ?> triggerAttributes;

    public void afterPropertiesSet() throws ParseException {
        // Create a trigger by the class name
        customTrigger = BeanUtils.instantiateClass(triggerClass);

        // If trigger is a standard trigger, set standard properties
        if (customTrigger instanceof AbstractTrigger) {
            AbstractTrigger<?> at = (AbstractTrigger<?>) customTrigger;

            if (jobDetail != null) {
                at.setJobKey(jobDetail.getKey());
            }

            if (triggerAttributes.containsKey(GrailsJobClassConstants.START_DELAY)) {
                Number startDelay = (Number) triggerAttributes.remove(GrailsJobClassConstants.START_DELAY);
                at.setStartTime(new Date(System.currentTimeMillis() + startDelay.longValue()));
            }
        }

        // Set non standard properties.
        BeanWrapper customTriggerWrapper = PropertyAccessorFactory.forBeanPropertyAccess(customTrigger);
        customTriggerWrapper.registerCustomEditor(String.class, new StringEditor());
        customTriggerWrapper.setPropertyValues(triggerAttributes);
    }

    public Trigger getObject() throws Exception {
        return customTrigger;
    }

    public Class<? extends Trigger> getObjectType() {
        return triggerClass;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void setTriggerClass(Class<Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }

    public void setTriggerAttributes(Map<?, ?> triggerAttributes) {
        this.triggerAttributes = triggerAttributes;
    }
}

// We need this additional editor to support GString -> String convertion for trigger's properties.
class StringEditor extends PropertyEditorSupport {
    @Override
    public void setValue(Object value) {
        super.setValue(value == null ? null : value.toString());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }
}
