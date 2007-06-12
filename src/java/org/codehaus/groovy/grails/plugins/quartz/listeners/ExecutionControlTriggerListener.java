/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.quartz.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

/**
 * TriggerListener implementation which prevents execution of jobs until
 * application will be fully started up.
 *
 * QuartzGrailsPlugin sets 'executionAllowed' flag to 'true' in
 * doWithApplicationContext closure which is executed after application
 * startup.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.2
 */
public class ExecutionControlTriggerListener extends TriggerListenerSupport {
	private static final transient Log LOG = LogFactory.getLog( ExecutionControlTriggerListener.class);

	public static final String NAME = "executionControlListener";

	private boolean executionAllowed = false;

	public String getName() {
		return NAME;
	}

    public boolean vetoJobExecution( Trigger trigger, JobExecutionContext jobExecutionContext ) {
        if( !executionAllowed && LOG.isDebugEnabled() ) 
            LOG.debug( "Job execution was canceled since application is not fully started up" );
        return !executionAllowed;
    }

    public void setExecutionAllowed( boolean executionAllowed ) {
        this.executionAllowed = executionAllowed;
    }
}