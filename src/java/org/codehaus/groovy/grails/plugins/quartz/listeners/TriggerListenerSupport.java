/* 
 * Copyright 2004-2006 OpenSymphony 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */
package org.codehaus.groovy.grails.plugins.quartz.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.TriggerListener;
import org.quartz.Trigger;
import org.quartz.JobExecutionContext;

/**
 * TODO: Remove this temporary while updating to Quartz 1.6 
 */
public abstract class TriggerListenerSupport implements TriggerListener {
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Get the <code>{@link org.apache.commons.logging.Log}</code> for this
     * class's category.  This should be used by subclasses for logging.
     */
    protected Log getLog() {
        return log;
    }

    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    }

    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    public void triggerMisfired(Trigger trigger) {
    }

    public void triggerComplete(
        Trigger trigger,
        JobExecutionContext context,
        int triggerInstructionCode) {
    }
}
