package grails.plugins.quartz

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import spock.lang.Specification

/**
 * Unit tests for JobDescriptor.
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
class JobDescriptorSpec extends Specification {

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

    void 'JobDescriptor builds correctly from a job and scheduler'() {
        when:
            JobDescriptor descriptor = JobDescriptor.build(job, scheduler)
        then:
            descriptor.name == 'job'
            descriptor.group == 'group'
            descriptor.triggerDescriptors.size() == 1
            descriptor.triggerDescriptors[0].name == 'trigger'
    }

}
