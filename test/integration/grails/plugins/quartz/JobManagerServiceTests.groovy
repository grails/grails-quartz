package grails.plugins.quartz

import static org.quartz.Trigger.TriggerState.NORMAL
import static org.quartz.Trigger.TriggerState.PAUSED
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

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
import org.quartz.impl.matchers.GroupMatcher

/**
 * Tests for the JobManagerService.
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
@TestMixin(IntegrationTestMixin)
class JobManagerServiceTests  {
    private static final JobKey JOB_KEY_11 = new JobKey("job1", "group1")
    private static final JobKey JOB_KEY_21 = new JobKey("job2", "group1")
    private static final JobKey JOB_KEY_12 = new JobKey("job1", "group2")
    private static final JobKey JOB_KEY_22 = new JobKey("job2", "group2")

    JobManagerService jobManagerService
    Scheduler quartzScheduler

    @Before
    void setUp() {
        JobDetail job1 = JobBuilder.newJob(SimpleTestJob).withIdentity(JOB_KEY_11).build()
        JobDetail job2 = JobBuilder.newJob(SimpleTestJob).withIdentity(JOB_KEY_21).build()
        JobDetail job3 = JobBuilder.newJob(SimpleTestJob).withIdentity(JOB_KEY_12).build()
        JobDetail job4 = JobBuilder.newJob(SimpleTestJob)
                .withIdentity(JOB_KEY_22)
                .storeDurably()
                .build()

        Trigger trigger1 = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey("trigger1", "tgroup1"))
                .build()

        Trigger trigger2 = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey("trigger2", "tgroup1"))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(2).repeatForever())
                .startNow()
                .build()

        Trigger trigger3 = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey("trigger3", "tgroup2"))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(3).repeatForever())
                .startNow()
                .build()

        Trigger trigger4 = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey("trigger4", "tgroup2"))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(4).repeatForever())
                .startNow()
                .build()

        quartzScheduler.scheduleJob(job1, trigger1)
        quartzScheduler.scheduleJob(job2, trigger2)
        quartzScheduler.scheduleJobs([(job3): [trigger3, trigger4] as Set], false)
        quartzScheduler.addJob(job4, false)
    }

    @After
    void tearDown() {
        quartzScheduler.deleteJob(JOB_KEY_11)
        quartzScheduler.deleteJob(JOB_KEY_12)
        quartzScheduler.deleteJob(JOB_KEY_21)
        quartzScheduler.deleteJob(JOB_KEY_22)
    }

    @Test
    void testInstantiate() {
        assert jobManagerService
    }

    @Test
    void testGetAllJobs() {
        Map<String, ? extends List<? extends JobDescriptor>> jobs = jobManagerService.getAllJobs()

        assert jobs instanceof Map
        assert jobs.size() == 2

        assert jobs.containsKey("group1")
        //assert jobs['group1'].size() == 1
        assert jobs['group1']*.name.contains('job2')

        assert jobs.containsKey("group2")
        assert jobs['group2'].size() == 2
        assert jobs['group2']*.name.contains('job1')
        assert jobs['group2']*.name.contains('job2')
    }

    @Test
    void testGetJobs() {
        assert jobManagerService.getJobs('group1')*.name.contains('job2')

        def names = jobManagerService.getJobs('group2')*.name
        assert names.contains('job1')
        assert names.contains('job2')
    }

    @Test
    void testGetRunningJobs() {
        jobManagerService.getRunningJobs()
    }

    @Test
    void testPauseAndResumeJob() {
        def triggerKeys = quartzScheduler.getTriggersOfJob(new JobKey('job2', 'group1'))*.key

        assertTriggersState(triggerKeys, NORMAL)
        jobManagerService.pauseJob('group1', 'job2')
        assertTriggersState(triggerKeys, PAUSED)
        jobManagerService.resumeJob('group1', 'job2')
        assertTriggersState(triggerKeys, NORMAL)
    }

    private void assertTriggersState(Iterable<TriggerKey> triggerKeys, Trigger.TriggerState state) {
        for (TriggerKey key : triggerKeys) {
            assert quartzScheduler.getTriggerState(key) == state
        }
    }

    @Test
    void testPauseAndResumeTrigger() {
        TriggerKey key = new TriggerKey('trigger2', 'tgroup1')

        assert quartzScheduler.getTriggerState(key) == NORMAL
        jobManagerService.pauseTrigger('tgroup1', 'trigger2')
        assert quartzScheduler.getTriggerState(key) == PAUSED
        jobManagerService.resumeTrigger('tgroup1', 'trigger2')
        assert quartzScheduler.getTriggerState(key) == NORMAL
    }

    @Test
    void testPauseAndResumeJobGroup() {
        def jobKeys = quartzScheduler.getJobKeys(GroupMatcher.groupEquals('group2'))
        def keys = []
        jobKeys.each {
            keys.addAll quartzScheduler.getTriggersOfJob(it)*.key
        }

        assertTriggersState(keys, NORMAL)
        jobManagerService.pauseJobGroup('group2')
        assertTriggersState(keys, PAUSED)
        jobManagerService.resumeJobGroup('group2')
        assertTriggersState(keys, NORMAL)
    }

    @Test
    void testPauseAndResumeTriggerGroup() {
        def keys = quartzScheduler.getTriggerKeys(GroupMatcher.groupEquals('tgroup2'))

        assertTriggersState(keys, NORMAL)
        jobManagerService.pauseTriggerGroup('tgroup2')
        assertTriggersState(keys, PAUSED)
        jobManagerService.resumeTriggerGroup('tgroup2')
        assertTriggersState(keys, NORMAL)
    }

    @Test
    void testRemoveJob() {
        assert jobManagerService.getJobs('group1')*.name.contains('job2')
        jobManagerService.removeJob('group1', 'job2')
        assert !jobManagerService.getJobs('group1')*.name.contains('job2')
    }

    @Test
    void testUnscheduleJob() {
        def key = new JobKey('job2', 'group1')
        assert quartzScheduler.getTriggersOfJob(key)

        jobManagerService.unscheduleJob('group1', 'job2')
        assert !quartzScheduler.getTriggersOfJob(key)
    }

    @Test
    void testInterruptJob() {
        jobManagerService.interruptJob('group1', 'job2')
    }

    @Test
    void testPauseAndResumeAll() {
        def keys = quartzScheduler.getTriggerKeys(GroupMatcher.groupEquals('tgroup2'))

        assertTriggersState(keys, NORMAL)
        jobManagerService.pauseAll()
        assertTriggersState(keys, PAUSED)
        jobManagerService.resumeAll()
        assertTriggersState(keys, NORMAL)
    }
}
