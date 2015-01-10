package grails.plugins.quartz

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.junit.Test
import org.quartz.JobDetail
import org.quartz.JobKey
import org.springframework.beans.BeanWrapper

/**
 * @author Vitalii Samolovskikh aka Kefir
 */
class JobDetailFactoryBeanTests {

    private static final String JOB_NAME = 'jobName'
    private static final String JOB_GROUP = 'jobGroup'
    private static final String JOB_DESCRIPTION = 'The job description'

    private JobDetailFactoryBean factory = new JobDetailFactoryBean()

    @Test
    void testFactory1() {
        factory.jobClass = new GrailsJobClassMock([
            fullName: JOB_NAME,
            group: JOB_GROUP,
            concurrent: true,
            durability: true,
            sessionRequired: true,
            requestsRecovery: true,
            description: JOB_DESCRIPTION
        ])
        factory.afterPropertiesSet()
        JobDetail jobDetail = factory.object
        assert new JobKey(JOB_NAME, JOB_GROUP) == jobDetail.key
        assert JOB_NAME == jobDetail.jobDataMap[JobDetailFactoryBean.JOB_NAME_PARAMETER]
        assert jobDetail.durable
        assert !jobDetail.concurrentExectionDisallowed
        assert !jobDetail.persistJobDataAfterExecution
        assert jobDetail.requestsRecovery()
        assert JOB_DESCRIPTION == jobDetail.description
    }

    @Test
    void testFactory2() {
        factory.jobClass = new GrailsJobClassMock([
            fullName: JOB_NAME,
            group: JOB_GROUP,
            concurrent: false,
            durability: false,
            sessionRequired: false,
            requestsRecovery: false
        ])
        factory.afterPropertiesSet()
        JobDetail jobDetail = factory.object
        assert new JobKey(JOB_NAME, JOB_GROUP) == jobDetail.key
        assert JOB_NAME == jobDetail.jobDataMap[JobDetailFactoryBean.JOB_NAME_PARAMETER]
        assert !jobDetail.durable
        assert jobDetail.concurrentExectionDisallowed
        assert jobDetail.persistJobDataAfterExecution
        assert !jobDetail.requestsRecovery()
        assert !jobDetail.description
    }
}

class GrailsJobClassMock implements GrailsJobClass {
    String group
    String fullName
    boolean concurrent
    boolean durability
    boolean sessionRequired
    boolean requestsRecovery
    String description

    void execute() {}
    Map<String, Object> getTriggers() {}
    boolean byName() { false }
    boolean byType() { false }
    boolean getAvailable() { false }
    boolean isAbstract() { false }
    GrailsApplication getGrailsApplication() {}
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
    void setGrailsApplication(GrailsApplication grailsApplication) {}
}
