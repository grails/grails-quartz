package grails.plugins.quartz

import grails.core.GrailsApplication
import org.quartz.JobDetail
import org.quartz.JobKey
import org.springframework.beans.BeanWrapper
import spock.lang.Specification

/**
 * Tests for the JobDetailFactoryBean
 *
 * @author Vitalii Samolovskikh aka Kefir
 */
class JobDetailFactoryBeanSpec extends Specification {

    private static final String JOB_NAME = 'jobName'
    private static final String JOB_GROUP = 'jobGroup'
    private static final String JOB_DESCRIPTION = 'The job description'
    JobDetailFactoryBean factory = new JobDetailFactoryBean()


    void 'testFactory1'() {
        setup:
            factory.jobClass = new GrailsJobClassMock(
                    [
                            fullName        : JOB_NAME,
                            group           : JOB_GROUP,
                            concurrent      : true,
                            durability      : true,
                            sessionRequired : true,
                            requestsRecovery: true,
                            description     : JOB_DESCRIPTION
                    ]
            )
            factory.afterPropertiesSet()
        when:
            JobDetail jobDetail = factory.object
        then:
            new JobKey(JOB_NAME, JOB_GROUP) == jobDetail.key
            JOB_NAME == jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER)
            jobDetail.durable
            !jobDetail.concurrentExectionDisallowed
            !jobDetail.persistJobDataAfterExecution
            jobDetail.requestsRecovery()
            JOB_DESCRIPTION == jobDetail.description
    }

    void 'testFactory2'() {
        setup:
            factory.jobClass = new GrailsJobClassMock(
                    [
                            fullName        : JOB_NAME,
                            group           : JOB_GROUP,
                            concurrent      : false,
                            durability      : false,
                            sessionRequired : false,
                            requestsRecovery: false
                    ]
            )
            factory.afterPropertiesSet()
        when:
            JobDetail jobDetail = factory.object
        then:
            new JobKey(JOB_NAME, JOB_GROUP) == jobDetail.key
            JOB_NAME == jobDetail.getJobDataMap().get(JobDetailFactoryBean.JOB_NAME_PARAMETER)
            !jobDetail.durable
            jobDetail.concurrentExectionDisallowed
            jobDetail.persistJobDataAfterExecution
            !jobDetail.requestsRecovery()
            jobDetail.description == null
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
