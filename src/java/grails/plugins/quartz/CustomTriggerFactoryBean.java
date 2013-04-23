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

import org.quartz.CalendarIntervalTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl;
import org.quartz.impl.triggers.CoreTrigger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import static org.quartz.DateBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 */
public class CustomTriggerFactoryBean implements FactoryBean, InitializingBean {
    private Class<Trigger> triggerClass;
    private Trigger customTrigger;
    private JobDetailImpl jobDetail;

    private Map triggerAttributes;

    public void afterPropertiesSet() throws ParseException {



        if (triggerAttributes.containsKey(GrailsJobClassConstants.START_DELAY)) {
            Number startDelay = (Number) triggerAttributes.remove(GrailsJobClassConstants.START_DELAY);

            if (jobDetail == null) {
                if (triggerAttributes.containsKey(GrailsJobClassConstants.CRON_EXPRESSION)) {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis() + startDelay.longValue()))
                            .forJob((String)triggerAttributes.get("name"))
                            .withSchedule(buildCronSchedule())
                            .build();

                } else {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis() + startDelay.longValue()))
                            .forJob((String)triggerAttributes.get("name"))
                            .build();
                }

            } else {
                if (triggerAttributes.containsKey(GrailsJobClassConstants.CRON_EXPRESSION)) {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis() + startDelay.longValue()))
                            .forJob(jobDetail)
                            .withSchedule(buildCronSchedule())
                            .build();
                }  else {
                customTrigger = newTrigger()
                        .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                        .startAt(new Date(System.currentTimeMillis() + startDelay.longValue()))
                        .forJob(jobDetail)
                        .build();
                }
            }

        } else {
            if (jobDetail != null) {
                if (triggerAttributes.containsKey(GrailsJobClassConstants.CRON_EXPRESSION)) {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis()))
                            .forJob(jobDetail)
                            .withSchedule(buildCronSchedule())
                            .build();
                } else {

                customTrigger = newTrigger()
                        .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                        .startAt(new Date(System.currentTimeMillis()))
                        .forJob(jobDetail)
                        .build();
                }
            } else {
                if (triggerAttributes.containsKey(GrailsJobClassConstants.CRON_EXPRESSION)) {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis()))
                            .forJob((String)triggerAttributes.get("name").toString())
                            .withSchedule(buildCronSchedule())
                            .build();

                } else {
                    customTrigger = newTrigger()
                            .withIdentity((String)triggerAttributes.get("name"),(String)triggerAttributes.get("group"))
                            .startAt(new Date(System.currentTimeMillis()))
                            .forJob((String)triggerAttributes.get("name").toString())
                            .build();
                }

            }
        }



        BeanWrapper customTriggerWrapper = PropertyAccessorFactory.forBeanPropertyAccess(customTrigger);
        customTriggerWrapper.registerCustomEditor(String.class, new StringEditor());
        customTriggerWrapper.setPropertyValues(triggerAttributes);
    }

    /**
     * Allow the cronExpression to be passed as either a string or instance of CronExpression
     *
     * @return an instance of CronScheduleBuilder
     */
    private CronScheduleBuilder buildCronSchedule() {
        if (triggerAttributes.get(GrailsJobClassConstants.CRON_EXPRESSION) instanceof CronExpression) {
            return cronSchedule((CronExpression)triggerAttributes.get(GrailsJobClassConstants.CRON_EXPRESSION));
        }
        return cronSchedule((String)triggerAttributes.get(GrailsJobClassConstants.CRON_EXPRESSION));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return customTrigger;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return triggerClass;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public void setJobDetail(JobDetailImpl jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void setTriggerClass(Class<Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }

    public void setTriggerAttributes(Map triggerAttributes) {
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