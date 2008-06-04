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

import org.codehaus.groovy.grails.commons.ApplicationAttributes;
import org.codehaus.groovy.grails.web.context.ServletContextHolder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Simplified version of Spring's <a href='http://static.springframework.org/spring/docs/2.5.x/api/org/springframework/scheduling/quartz/MethodInvokingJobDetailFactoryBean.html'>MethodInvokingJobDetailFactoryBean</a>
 * that avoids issues with non-serializable classes (for JDBC storage).
 *
 * @author <a href='mailto:beckwithb@studentsonly.com'>Burt Beckwith</a>
 */
public class JobDetailFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

	private String name;
	private String group;
	private boolean concurrent = true;
	private String[] jobListenerNames;
	private JobDetail jobDetail;
	private String grailsJobName;

    /**
	 * Set the full name of the Job artifact.
	 *
	 * @param grailsJobName  the name
	 */
	public void setGrailsJobName(final String grailsJobName) {
		this.grailsJobName = grailsJobName;
	}

	/**
	 * Set the name of the job.
	 * <p>Default is the bean name of this FactoryBean.
     *
	 * @param name name of the job
     *
     * @see org.quartz.JobDetail#setName
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Set the group of the job.
	 * <p>Default is the default group of the Scheduler.
     *
	 * @param group group name of the job
     *
     * @see org.quartz.JobDetail#setGroup
	 * @see org.quartz.Scheduler#DEFAULT_GROUP
	 */
	public void setGroup(final String group) {
		this.group = group;
	}

	/**
	 * Specify whether or not multiple jobs should be run in a concurrent
	 * fashion. The behavior when one does not want concurrent jobs to be
	 * executed is realized through adding the {@link StatefulJob} interface.
	 * More information on stateful versus stateless jobs can be found
	 * <a href="http://www.opensymphony.com/quartz/tutorial.html#jobsMore">here</a>.
	 * <p>The default setting is to run jobs concurrently.
     *
     * @param concurrent true for concurrent runs
     */
	public void setConcurrent(final boolean concurrent) {
		this.concurrent = concurrent;
	}

	/**
	 * Set a list of JobListener names for this job, referring to
	 * non-global JobListeners registered with the Scheduler.
	 * <p>A JobListener name always refers to the name returned
	 * by the JobListener implementation.
     *
	 * @param names array of job listener names which should be applied to the job
     *
     * @see SchedulerFactoryBean#setJobListeners
	 * @see org.quartz.JobListener#getName
	 */
	public void setJobListenerNames(final String[] names) {
		this.jobListenerNames = names;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {

		if (name == null) {
			throw new IllegalStateException("name is required");
		}

		if (group == null) {
			throw new IllegalStateException("group is required");
		}

		if (grailsJobName == null) {
			throw new IllegalStateException("grailsJobName is required");
		}

		// Consider the concurrent flag to choose between stateful and stateless job.
		Class jobClass = (concurrent ? GrailsTaskClassJob.class : StatefulGrailsTaskClassJob.class);

		// Build JobDetail instance.
		jobDetail = new JobDetail(name, group, jobClass);
        jobDetail.getJobDataMap().put("applicationContext", applicationContext);
        jobDetail.getJobDataMap().put("grailsJobName", grailsJobName);
		jobDetail.setVolatility(true);
		jobDetail.setDurability(true);

		// Register job listener names.
		if (jobListenerNames != null) {
			for (int i = 0; i < jobListenerNames.length; i++) {
				jobDetail.addJobListener(jobListenerNames[i]);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() {
		return jobDetail;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return JobDetail.class;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
	 * Quartz Job implementation that invokes execute() on the GrailsTaskClass instance.
	 */
	public static class GrailsTaskClassJob implements Job {
		public void execute(final JobExecutionContext context) throws JobExecutionException {
			try {
				String grailsJobName = (String) context.getMergedJobDataMap().get("grailsJobName");
                ApplicationContext ctx = (ApplicationContext) context.getMergedJobDataMap().get("applicationContext");
                Object job = ctx.getBean(grailsJobName);
                // trying to invoke execute(context) method
                Method method = ReflectionUtils.findMethod(job.getClass(), "execute", new Class[] {Object.class});
                if(method != null) {
                    ReflectionUtils.invokeMethod(method, job, new Object[] {context});
                } else {
                    // falling back to execute() method
                    ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(job.getClass(), "execute"), job);
                }
			}
			catch (Exception e) {
				throw new JobExecutionException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Extension of the GrailsTaskClassJob, implementing the StatefulJob interface.
	 * Quartz checks whether or not jobs are stateful and if so,
	 * won't let jobs interfere with each other.
	 */
	public static class StatefulGrailsTaskClassJob extends GrailsTaskClassJob implements StatefulJob {
		// No implementation, just an addition of the tag interface StatefulJob
		// in order to allow stateful jobs.
	}
}
