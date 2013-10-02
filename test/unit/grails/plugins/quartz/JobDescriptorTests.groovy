package grails.plugins.quartz

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.StdSchedulerFactory

/**
 * Unit tests for JobDescriptor.
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
class JobDescriptorTests {
    private Scheduler scheduler
    private JobDetail job
    private Trigger trigger

    @Before
    void prepare(){
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

    @After
    void dispose(){
        scheduler.shutdown()
    }

    @Test
    void testBuild(){
        JobDescriptor descriptor = JobDescriptor.build(job, scheduler)
        assert descriptor.name == 'job'
        assert descriptor.group == 'group'
        assert descriptor.triggerDescriptors.size() == 1
        assert descriptor.triggerDescriptors[0].name == 'trigger'
    }
}
