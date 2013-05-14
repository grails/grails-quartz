package grails.plugins.quartz

import grails.test.mixin.TestFor
import org.junit.*
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory

/**
 * Tests for JobManagerService
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
@TestFor(JobManagerService)
class JobManagerServiceTests {
    Scheduler scheduler

    @Before
    public void createScheduler(){
        scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        service.quartzScheduler = scheduler
    }

    @After
    public void shutdownScheduler(){
        scheduler.shutdown()
    }

    @Test
    public void testGetAllJobs(){
        def jobs = service.getAllJobs()
        assertNotNull(jobs)
        assert jobs instanceof Map
        assert jobs.size() == 0
    }
}
