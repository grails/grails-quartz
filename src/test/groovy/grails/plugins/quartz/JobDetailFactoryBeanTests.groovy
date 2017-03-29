package grails.plugins.quartz

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertFalse
import static junit.framework.Assert.assertNull
import static junit.framework.Assert.assertTrue

import grails.core.GrailsApplication
import org.junit.Test
import org.quartz.JobDetail
import org.quartz.JobKey
import org.springframework.beans.BeanWrapper

/**
 * Tests for the JobDetailFactoryBean
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
class JobDetailFactoryBeanTests {
    private static final String JOB_NAME = 'jobName'
    private static final String JOB_GROUP = 'jobGroup'
    private static final String JOB_DESCRIPTION = 'The job description'
    JobDetailFactoryBean factory = new JobDetailFactoryBean()

    @Test
    void testFactory1(){
        factory.jobClass = new GrailsJobClassMock(
                [
                        fullName:JOB_NAME,
                        group:JOB_GROUP,
                        concurrent:true,
                        durability:true,
                        sessionRequired:true,
                        requestsRecovery:true,
                        description: JOB_DESCRIPTION
                ]
        )
        factory.afterPropertiesSet()
        JobDetail jobDetail = factory.object
        assertEquals(new JobKey(JOB_NAME, JOB_GROUP), jobDetail.key)
        assertEquals(JOB_NAME, jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER))
        assertTrue(jobDetail.durable)
        assertFalse(jobDetail.concurrentExectionDisallowed)
        assertFalse(jobDetail.persistJobDataAfterExecution)
        assertTrue(jobDetail.requestsRecovery())
        assertEquals(JOB_DESCRIPTION, jobDetail.description)
    }

    @Test
    void testFactory2(){
        factory.jobClass = new GrailsJobClassMock(
                [
                        fullName:JOB_NAME,
                        group:JOB_GROUP,
                        concurrent:false,
                        durability:false,
                        sessionRequired:false,
                        requestsRecovery:false
                ]
        )
        factory.afterPropertiesSet()
        JobDetail jobDetail = factory.object
        assertEquals(new JobKey(JOB_NAME, JOB_GROUP), jobDetail.key)
        assertEquals(JOB_NAME, jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER))
        assertFalse(jobDetail.durable)
        assertTrue(jobDetail.concurrentExectionDisallowed)
        assertTrue(jobDetail.persistJobDataAfterExecution)
        assertFalse(jobDetail.requestsRecovery())
        assertNull(jobDetail.description)
    }
}

class GrailsJobClassMock implements GrailsJobClass {
    String group
    String fullName
    boolean concurrent
	boolean jobEnabled
    boolean durability
    boolean sessionRequired
    boolean requestsRecovery
    String description

    void execute() {}
    Map getTriggers() {}
    boolean byName() { false }
    boolean byType() { false }
    boolean getAvailable() { false }
    boolean isAbstract() { false }
	boolean isEnabled() { true }
    GrailsApplication getGrailsApplication() {}

    @Override
    grails.core.GrailsApplication getApplication() {
        return null
    }

    Object getPropertyValue(String name) {}
    boolean hasProperty(String name) { false }
    Object newInstance() {}
    String getName() {}
    String getShortName() {}
    String getPropertyName() {}
    String getLogicalPropertyName() {}
    String getNaturalName() {}
    String getPackageName() {}
    Class getClazz() {}
    BeanWrapper getReference() {}
    Object getReferenceInstance() {}
    def <T> T getPropertyValue(String name, Class<T> type) {}

    @Override
    String getPluginName() {
        return null
    }

    void setGrailsApplication(GrailsApplication grailsApplication) {}
}
