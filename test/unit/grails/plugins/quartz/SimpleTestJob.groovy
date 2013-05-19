package grails.plugins.quartz

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.UnableToInterruptJobException

/**
 * The job for tests. Do nothing.
 */
class SimpleTestJob implements Job, InterruptableJob {
    private final Log log = LogFactory.getLog(SimpleTestJob)

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.trace("Executing simple job. Thread=${Thread.currentThread().id}.")
    }

    @Override
    void interrupt() throws UnableToInterruptJobException {
        log.trace("Interrupt simple job. Thread=${Thread.currentThread().id}.")
    }
}