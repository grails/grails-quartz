package grails.plugins.quartz

import grails.test.mixin.TestFor
import org.junit.*
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher

import static org.quartz.Trigger.TriggerState.*

/**
 * Tests for JobManagerService
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
@TestFor(JobManagerService)
class JobManagerServiceTests {
    Scheduler scheduler

    /**
     * Create the quartz scheduler and prepare a few jobs and triggers.
     */
    @Before
    public void createScheduler() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()

        scheduler.start()
        service.quartzScheduler = scheduler

        JobDetail job1 = JobBuilder.newJob(SimpleTestJob.class).withIdentity(new JobKey("job1", "group1")).build()
        JobDetail job2 = JobBuilder.newJob(SimpleTestJob.class).withIdentity(new JobKey("job2", "group1")).build()
        JobDetail job3 = JobBuilder.newJob(SimpleTestJob.class).withIdentity(new JobKey("job1", "group2")).build()
        JobDetail job4 = JobBuilder.newJob(SimpleTestJob.class)
                .withIdentity(new JobKey("job2", "group2"))
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

        scheduler.scheduleJob(job1, trigger1)
        scheduler.scheduleJob(job2, trigger2)
        scheduler.scheduleJobs([(job3): [trigger3, trigger4]], false)
        scheduler.addJob(job4, false)
    }

    /**
     * Shutdown the scheduler
     */
    @After
    public void shutdownScheduler() {
        scheduler.shutdown()
    }

    @Test
    public void testGetAllJobs() {
        Map<String, ? extends List<? extends JobDescriptor>> jobs = service.getAllJobs()

        assertNotNull(jobs)

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
    public void testGetJobs() {
        assert service.getJobs('group1')*.name.contains('job2')

        def names = service.getJobs('group2')*.name
        assert names.contains('job1')
        assert names.contains('job2')
    }

    @Test
    public void testGetRunningJobs() {
        service.getRunningJobs()
    }

    @Test
    public void testPauseAndResumeJob() {
        def triggerKeys = scheduler.getTriggersOfJob(new JobKey('job2', 'group1'))*.key

        assertTriggersState(triggerKeys, NORMAL)
        service.pauseJob('group1', 'job2')
        assertTriggersState(triggerKeys, PAUSED)
        service.resumeJob('group1', 'job2')
        assertTriggersState(triggerKeys, NORMAL)
    }

    private void assertTriggersState(Iterable<TriggerKey> triggerKeys, Trigger.TriggerState state) {
        for (TriggerKey key : triggerKeys) {
            assert scheduler.getTriggerState(key) == state
        }
    }

    @Test
    public void PauseAndResumeTrigger() {
        TriggerKey key = new TriggerKey('trigger2', 'tgroup1')

        assert scheduler.getTriggerState(key) == NORMAL
        service.pauseTrigger('tgroup1', 'trigger2')
        assert scheduler.getTriggerState(key) == PAUSED
        service.resumeTrigger('tgroup1', 'trigger2')
        assert scheduler.getTriggerState(key) == NORMAL
    }

    @Test
    public void testPauseAndResumeJobGroup() {
        def jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals('group2'))
        def keys = []
        jobKeys.each {
            keys += scheduler.getTriggersOfJob(it)*.key
        }

        assertTriggersState(keys, NORMAL)
        service.pauseJobGroup('group2');
        assertTriggersState(keys, PAUSED)
        service.resumeJobGroup('group2');
        assertTriggersState(keys, NORMAL)
    }

    @Test
    public void testPauseAndResumeTriggerGroup() {
        def keys = scheduler.getTriggerKeys(GroupMatcher.groupEquals('tgroup2'))

        assertTriggersState(keys, NORMAL)
        service.pauseTriggerGroup('tgroup2');
        assertTriggersState(keys, PAUSED)
        service.resumeTriggerGroup('tgroup2');
        assertTriggersState(keys, NORMAL)
    }

    @Test
    public void testRemoveJob() {
        assertTrue(service.getJobs('group1')*.name.contains('job2'))
        service.removeJob('group1', 'job2')
        assertFalse(service.getJobs('group1')*.name.contains('job2'))
    }

    @Test
    public void testUnscheduleJob() {
        def key = new JobKey('job2', 'group1')
        assert scheduler.getTriggersOfJob(key)?.size() > 0
        service.unscheduleJob('group1', 'job2')
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(key)
        assert triggers == null || triggers.size() == 0
    }

    @Test
    public void testInterruptJob(){
        service.interruptJob('group1', 'job2')
    }

    @Test
    public void testPauseAndResumeAll(){
        def keys = scheduler.getTriggerKeys(GroupMatcher.groupEquals('tgroup2'))

        assertTriggersState(keys, NORMAL)
        service.pauseAll()
        assertTriggersState(keys, PAUSED)
        service.resumeAll()
        assertTriggersState(keys, NORMAL)
    }
}
