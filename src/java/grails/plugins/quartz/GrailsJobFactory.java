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

import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Job factory which retrieves Job instances from ApplicationContext.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.3.2
 */
public class GrailsJobFactory extends AdaptableJobFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        String grailsJobName = (String) bundle.getJobDetail().getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER);
        if (grailsJobName != null) {
            Object job = applicationContext.getBean(grailsJobName);
            if (bundle.getJobDetail().getJobClass().equals(StatefulGrailsJob.class)) {
                return new StatefulGrailsJob(job);
            }
            return new GrailsJob(job);
        } else {
            return super.createJobInstance(bundle);
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Quartz Job implementation that invokes execute() on the application's job class.
     */
    public class GrailsJob implements InterruptableJob {
        Object job;
        Method executeMethod;
        Method interruptMethod;
        boolean passExecutionContext;

        public GrailsJob(Object job) {
            this.job = job;
            this.executeMethod = ReflectionUtils.findMethod(job.getClass(), GrailsJobClassConstants.EXECUTE, (Class<?>[]) null);
            if (executeMethod == null) {
                throw new IllegalArgumentException(job.getClass().getName() + " should declare #execute() method");
            }
            switch (executeMethod.getParameterTypes().length) {
                case 0:
                    passExecutionContext = false;
                    break;
                case 1:
                    passExecutionContext = true;
                    break;
                default:
                    throw new IllegalArgumentException(job.getClass().getName() + "#execute() method should take either no arguments or one argument of type JobExecutionContext");
            }
            this.interruptMethod = ReflectionUtils.findMethod(job.getClass(), GrailsJobClassConstants.INTERRUPT);
        }

        public void execute(final JobExecutionContext context) throws JobExecutionException {
            try {
                if (passExecutionContext) {
                    executeMethod.invoke(job, context);
                } else {
                    executeMethod.invoke(job);
                }
            } catch (InvocationTargetException ite) {
                Throwable targetException = ite.getTargetException();
                if (targetException instanceof JobExecutionException) {
                    throw (JobExecutionException) targetException;
                } else {
                    throw new JobExecutionException(targetException);
                }
            } catch (IllegalAccessException iae) {
                JobExecutionException criticalError = new JobExecutionException("Cannot invoke " + job.getClass().getName() + "#execute() method", iae);
                criticalError.setUnscheduleAllTriggers(true);
                throw criticalError;
            }
        }

        public void interrupt() throws UnableToInterruptJobException {
            if (interruptMethod != null) {
                try {
                    interruptMethod.invoke(job);
                } catch (Throwable e) {
                    throw new UnableToInterruptJobException(e);
                }
            } else {
                throw new UnableToInterruptJobException(job.getClass().getName() + " doesn't support interruption");
            }
        }
    }

    /**
     * Extension of the GrailsJob, implementing the StatefulJob interface.
     * Quartz checks whether or not jobs are stateful and if so,
     * won't let jobs interfere with each other.
     */
    public class StatefulGrailsJob extends GrailsJob implements StatefulJob {
        // No implementation, just an addition of the tag interface StatefulJob
        // in order to allow stateful jobs.

        public StatefulGrailsJob(Object job) {
            super(job);
        }
    }

}
