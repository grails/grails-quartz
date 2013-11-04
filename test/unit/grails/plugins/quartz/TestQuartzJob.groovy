package grails.plugins.quartz

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * @author Vitalii Samolovskikh aka Kefir
 */
class TestQuartzJob implements Job {
    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {}
}
