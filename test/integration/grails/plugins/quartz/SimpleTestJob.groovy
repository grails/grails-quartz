package grails.plugins.quartz

import org.quartz.InterruptableJob
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.UnableToInterruptJobException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The job for tests. Do nothing.
 */
class SimpleTestJob implements InterruptableJob {
    protected final Logger log = LoggerFactory.getLogger(getClass())

    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.trace("Executing simple job. Thread=${Thread.currentThread().id}.")
    }

    void interrupt() throws UnableToInterruptJobException {
        log.trace("Interrupt simple job. Thread=${Thread.currentThread().id}.")
    }
}
