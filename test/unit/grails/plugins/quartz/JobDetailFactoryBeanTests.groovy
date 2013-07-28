package grails.plugins.quartz

import junit.framework.Assert
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.junit.Before
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
    JobDetailFactoryBean factory

    @Before
    void createFactory(){
        factory = new JobDetailFactoryBean()
    }

    @Test
    void testFactory1(){
        factory.jobClass = new GrailsJobClassMock(
                [
                        fullName:JOB_NAME,
                        group:JOB_GROUP,
                        concurrent:true,
                        durability:true,
                        sessionRequired:true,
                        requestsRecovery:true
                ]
        )
        factory.afterPropertiesSet()
        JobDetail jobDetail = factory.object
        Assert.assertEquals(new JobKey(JOB_NAME, JOB_GROUP), jobDetail.key)
        Assert.assertEquals(JOB_NAME, jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER))
        Assert.assertTrue(jobDetail.durable)
        Assert.assertFalse(jobDetail.concurrentExectionDisallowed)
        Assert.assertFalse(jobDetail.persistJobDataAfterExecution)
        Assert.assertTrue(jobDetail.requestsRecovery())
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
        Assert.assertEquals(new JobKey(JOB_NAME, JOB_GROUP), jobDetail.key)
        Assert.assertEquals(JOB_NAME, jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER))
        Assert.assertFalse(jobDetail.durable)
        Assert.assertTrue(jobDetail.concurrentExectionDisallowed)
        Assert.assertTrue(jobDetail.persistJobDataAfterExecution)
        Assert.assertFalse(jobDetail.requestsRecovery())
    }
}

class GrailsJobClassMock implements GrailsJobClass {
    String group
    String fullName
    boolean concurrent
    boolean durability
    boolean sessionRequired
    boolean requestsRecovery

    @Override
    void execute() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Map getTriggers() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean byName() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean byType() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean getAvailable() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean isAbstract() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    GrailsApplication getGrailsApplication() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Object getPropertyValue(String name) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean hasProperty(String name) {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Object newInstance() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getShortName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getPropertyName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getLogicalPropertyName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getNaturalName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getPackageName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Class getClazz() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    BeanWrapper getReference() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Object getReferenceInstance() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    def <T> T getPropertyValue(String name, Class<T> type) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
