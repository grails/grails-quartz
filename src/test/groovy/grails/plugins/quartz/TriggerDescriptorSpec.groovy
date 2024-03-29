package grails.plugins.quartz

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.StdSchedulerFactory
import spock.lang.Specification

/**
 * Unit tests for TriggerDescriptor
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
class TriggerDescriptorSpec extends Specification {

    private Scheduler scheduler
    private JobDetail job
    private Trigger trigger

    def setup() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()

        scheduler.start()

        job = JobBuilder.newJob(TestQuartzJob).withIdentity(new JobKey("job", "group")).build()
        trigger = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey("trigger", "group"))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(2).repeatForever())
                .startNow()
                .build()

        scheduler.scheduleJob(job, trigger)
    }

    def cleanup() {
        scheduler.shutdown()
    }


    void 'build TriggerDescriptor correctly'() {
        when:
            TriggerDescriptor descriptor =
                    TriggerDescriptor.build(JobDescriptor.build(job, scheduler), trigger, scheduler)
        then:
            descriptor.name == 'trigger'
            descriptor.group == 'group'
            descriptor.state == Trigger.TriggerState.NORMAL
    }
}
