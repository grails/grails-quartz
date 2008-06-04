package org.codehaus.groovy.grails.plugins.quartz;

import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.beans.BeansException;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.lang.reflect.Method;

/**
 * Job factory which retrieves Job instances from ApplicationContext.
 */
public class GrailsJobFactory extends AdaptableJobFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        String grailsJobName = (String) bundle.getJobDetail().getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER);
        Object job = applicationContext.getBean(grailsJobName);
        if(bundle.getJobDetail().getJobClass().equals(StatefulGrailsTaskClassJob.class)) {
            return new StatefulGrailsTaskClassJob(job);
        }
        return new GrailsTaskClassJob(job);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
	 * Quartz Job implementation that invokes execute() on the GrailsTaskClass instance.
	 */
	public class GrailsTaskClassJob implements Job {
        Object job;

        public GrailsTaskClassJob(Object job) {
            this.job = job;
        }

        public void execute(final JobExecutionContext context) throws JobExecutionException {
			try {
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
	public class StatefulGrailsTaskClassJob extends GrailsTaskClassJob implements StatefulJob {
		// No implementation, just an addition of the tag interface StatefulJob
		// in order to allow stateful jobs.

        public StatefulGrailsTaskClassJob(Object job) {
            super(job);
        }
    }

}
