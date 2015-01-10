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

import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

import java.util.Map;

/**
 * Represents a job class.
 *
 * @author Micha?? K??ujszo
 * @author Graeme Rocher
 * @author Marcel Overdijk
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.1
 */
public interface GrailsJobClass extends InjectableGrailsClass {

    /**
     * Executed by the job scheduler.
     */
    void execute();

    /**
     * Group name used for configuring scheduler.
     *
     * @return jobs group name for this job
     */
    String getGroup();

    /**
     * Determine if jobs can be executed concurrently.
     *
     * @return true if multiple instances of this job can run concurrently
     */
    boolean isConcurrent();

    /**
     * Determine if job requires Hibernate Session bound to thread.
     *
     * @return true if this job requires a Hibernate Session bound to thread
     */
    boolean isSessionRequired();

    /**
     * Determine if job is durable.
     *
     * @return true if this job is durable
     */
    boolean isDurability();

    /**
     * Determine if job should be re-executed if a 'recovery' or 'fail-over' situation is encountered.
     *
     * @return true if this job requests recovery
     */
    boolean isRequestsRecovery();

    /**
     * Job's description for configuring job details.
     *
     * @return description
     */
    String getDescription();

    Map<String, Object> getTriggers();
}
