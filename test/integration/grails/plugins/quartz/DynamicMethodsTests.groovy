package grails.plugins.quartz

import static org.quartz.JobBuilder.newJob
import static org.quartz.TriggerBuilder.*

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.quartz.CronTrigger
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerKey
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*
import org.junit.*
import static org.junit.Assert.*

/**
 * Tests the dynamic methods of jobs.
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
@TestMixin(IntegrationTestMixin)
class DynamicMethodsTests  {
    private static final long REPEAT_INTERVAL = 1000l
    private static final String CRON_EXPRESSION = '0/20 * * * * ?'
    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager
    Scheduler quartzScheduler
    private JobDetail jobDetail

    @Test
    void testTriggerNow() {
        IntegrationTestJob.triggerNow()
    }

    @Test
    void testScheduleSimple() {
        IntegrationTestJob.schedule(REPEAT_INTERVAL)
        List<Trigger> triggers = quartzScheduler.getTriggersOfJob(jobDetail.key)
        assert triggers.size() == 1
        SimpleTrigger trigger = triggers.get(0) as SimpleTrigger
        assertEquals(REPEAT_INTERVAL, trigger.repeatInterval)
        assertEquals(SimpleTrigger.REPEAT_INDEFINITELY, trigger.repeatCount)
    }

    @Test
    void testScheduleCron() {
        IntegrationTestJob.schedule(CRON_EXPRESSION)
        List<Trigger> triggers = quartzScheduler.getTriggersOfJob(jobDetail.key)
        assert triggers.size() == 1
        CronTrigger trigger = triggers.get(0) as CronTrigger
        assertEquals(CRON_EXPRESSION, trigger.cronExpression)
    }

    @Test
    void testScheduleDate() {
        IntegrationTestJob.schedule(new Date(new Date().getTime() + 60000l))
        assert quartzScheduler.getTriggersOfJob(jobDetail.key).size() == 1
    }

    @Test
    void testScheduleTrigger() {
        IntegrationTestJob.schedule(newTrigger().startNow().build())
        assert quartzScheduler.getTriggersOfJob(jobDetail.key).size() == 1
    }

    @Test
    void testRemoveJob() {
        IntegrationTestJob.removeJob()
        assertNull(quartzScheduler.getJobDetail(jobDetail.key))
    }

    @Test
    void testRescheduleJob() {
        assertEquals(0, quartzScheduler.getTriggersOfJob(jobDetail.key).size())
        Trigger trigger = newTrigger()
                .withIdentity(new TriggerKey("trigger", "group"))
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever()).build()
        IntegrationTestJob.schedule(trigger)
        assert quartzScheduler.getTriggersOfJob(jobDetail.key).size() == 1
        IntegrationTestJob.reschedule(trigger)
        assert quartzScheduler.getTriggersOfJob(jobDetail.key).size() == 1
    }

    @Test
    void testUnscheduleJob() {
        assertEquals(0, quartzScheduler.getTriggersOfJob(jobDetail.key).size())
        Trigger trigger = newTrigger()
                .withIdentity(new TriggerKey("trigger", "group"))
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever()).build()
        IntegrationTestJob.schedule(trigger)
        assert quartzScheduler.getTriggersOfJob(jobDetail.key).size() == 1
        IntegrationTestJob.unschedule(trigger.key.name, trigger.key.group)
        assertEquals(0, quartzScheduler.getTriggersOfJob(jobDetail.key).size())
    }

    @Before
    void setUp() {
        grailsApplication.addArtefact(JobArtefactHandler.TYPE, IntegrationTestJob)
        pluginManager.getGrailsPlugin('quartz')
        def plugin = pluginManager.getGrailsPlugin("quartz")
        plugin.doWithDynamicMethods(grailsApplication.mainContext)
        GrailsJobClass jobClass = grailsApplication.getJobClass(IntegrationTestJob.name)
        jobDetail = newJob(SimpleTestJob).withIdentity(jobClass.getFullName(), jobClass.getGroup()).storeDurably().build()
        quartzScheduler.addJob(jobDetail, true)
    }

    @After
    void tearDown() {
        quartzScheduler.deleteJob(jobDetail.key)
    }
}
