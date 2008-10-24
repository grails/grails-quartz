import org.quartz.Scheduler
import org.quartz.CronTrigger
import org.quartz.Trigger
import org.quartz.JobDetail

class JobManagerController {

    Scheduler quartzScheduler
    def jobManagerService

    def index = {        
        JobDetail jobDetail1 = new JobDetail("FooJob1", "FooGroup1", org.codehaus.groovy.grails.plugins.quartz.FooJob.class)
        JobDetail jobDetail2 = new JobDetail("FooJob2", "FooGroup1", org.codehaus.groovy.grails.plugins.quartz.FooJob.class)
        JobDetail jobDetail3 = new JobDetail("FooJob3", "FooGroup2", org.codehaus.groovy.grails.plugins.quartz.FooJob.class)
        JobDetail jobDetail4 = new JobDetail("FooJob1", "FooGroup2", org.codehaus.groovy.grails.plugins.quartz.FooJob.class)
        quartzScheduler.addJob (jobDetail1, true)
        quartzScheduler.addJob (jobDetail2, true)
        quartzScheduler.addJob (jobDetail3, true)
        quartzScheduler.addJob (jobDetail4, true)
        
        Trigger trigger1 = new CronTrigger("TR1", "G1", "0/7 * * * * ?")
        trigger1.setJobName("FooJob1")
        trigger1.setJobGroup("FooGroup1")
        quartzScheduler.scheduleJob(trigger1)

        Trigger trigger2 = new CronTrigger("TR2", "G1", "0/12 * * * * ?")
        trigger2.setJobName("FooJob2")
        trigger2.setJobGroup("FooGroup1")
        quartzScheduler.scheduleJob(trigger2)

        Trigger trigger3 = new CronTrigger("TR3", "G1", "0/2 * * * * ?")
        trigger3.setJobName("FooJob3")
        trigger3.setJobGroup("FooGroup2")
        quartzScheduler.scheduleJob(trigger3)

        Trigger trigger4 = new CronTrigger("TR4", "G1", "0/8 * * * * ?")
        trigger4.setJobName("FooJob1")
        trigger4.setJobGroup("FooGroup2")
        quartzScheduler.scheduleJob(trigger4)

        redirect(action: list, params: params)
    }

    def list = {
        ["jobsList":jobManagerService.getAllJobs()]
    }

    def pause = {
        if (params.what?.equals("JOB")) {
            quartzScheduler.pauseJob (params.jobName, params.jobGroup)
        } else if (params.what?.equals("TRIGGER")) {
            quartzScheduler.pauseTrigger (params.triggerName, params.triggerGroup)
        }
        redirect(action: list, params: [:])
    }

    def resume = {
        if (params.what?.equals("JOB")) {
            quartzScheduler.resumeJob (params.jobName, params.jobGroup)
        } else if (params.what?.equals("TRIGGER")) {
            println "Resuming Trigger: ${params.triggerName}-${params.triggerGroup}"
            quartzScheduler.resumeTrigger (params.triggerName, params.triggerGroup)
        }
        redirect(action: list, params: [:])
    }
}
