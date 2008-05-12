/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Simplified version of Spring's <a href='http://static.springframework.org/spring/docs/2.5.x/api/org/springframework/scheduling/quartz/SimpleTriggerBean.html'>SimpleTriggerBean</a>
 * that is serializable (for JDBC storage).
 *
 * @author Juergen Hoeller
 * @author <a href='mailto:beckwithb@studentsonly.com'>Burt Beckwith</a>
 */
public class GrailsSimpleTriggerBean
       extends SimpleTrigger
       implements JobDetailAwareTrigger, BeanNameAware, InitializingBean, Serializable {

	private static final long serialVersionUID = 3320172197966889670L;

	private long startDelay;
	private JobDetail jobDetail;
	private String beanName;

	public GrailsSimpleTriggerBean() {
		setRepeatCount(REPEAT_INDEFINITELY);
	}

	/**
	 * Register objects in the JobDataMap via a given Map.
	 * <p>
	 * These objects will be available to this Trigger only, in contrast to
	 * objects in the JobDetail's data map.
	 * 
	 * @param jobDataAsMap
	 *           Map with String keys and any objects as values (for example
	 *           Spring-managed beans)
	 * @see JobDetailBean#setJobDataAsMap
	 */
	public void setJobDataAsMap(final Map jobDataAsMap) {
		getJobDataMap().putAll(jobDataAsMap);
	}

	/**
	 * Set a list of TriggerListener names for this job, referring to non-global
	 * TriggerListeners registered with the Scheduler.
	 * <p>
	 * A TriggerListener name always refers to the name returned by the
	 * TriggerListener implementation.
	 * 
	 * @see SchedulerFactoryBean#setTriggerListeners
	 * @see org.quartz.TriggerListener#getName
	 */
	public void setTriggerListenerNames(final String[] names) {
		for (int i = 0; i < names.length; i++) {
			addTriggerListener(names[i]);
		}
	}

	/**
	 * Set the delay before starting the job for the first time. The given number
	 * of milliseconds will be added to the current time to calculate the start
	 * time. Default is 0.
	 * <p>
	 * This delay will just be applied if no custom start time was specified.
	 * However, in typical usage within a Spring context, the start time will be
	 * the container startup time anyway. Specifying a relative delay is
	 * appropriate in that case.
	 * 
	 * @see #setStartTime
	 */
	public void setStartDelay(final long startDelay) {
		this.startDelay = startDelay;
	}

	/**
	 * Set the JobDetail that this trigger should be associated with.
	 * <p>
	 * This is typically used with a bean reference if the JobDetail is a
	 * Spring-managed bean. Alternatively, the trigger can also be associated
	 * with a job by name and group.
	 * 
	 * @see #setJobName
	 * @see #setJobGroup
	 */
	public void setJobDetail(final JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.scheduling.quartz.JobDetailAwareTrigger#getJobDetail()
	 */
	public JobDetail getJobDetail() {
		return this.jobDetail;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		if (getName() == null) {
			setName(beanName);
		}
		if (getGroup() == null) {
			setGroup(Scheduler.DEFAULT_GROUP);
		}
		if (getStartTime() == null) {
			setStartTime(new Date(System.currentTimeMillis() + startDelay));
		}
		if (jobDetail != null) {
			setJobName(jobDetail.getName());
			setJobGroup(jobDetail.getGroup());
		}
		setVolatility(true);
	}
}
