/*
 * Copyright (c) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.quartz

import org.quartz.Scheduler

/**
 * JobManagerService simplifies interaction with the Quartz Scheduler from Grails application. 
 *
 * @author Marco Mornati (mmornati@byte-code.com)
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 *
 * @since 0.4
 */
class JobManagerService {

    boolean transactional = false

    Scheduler quartzScheduler

    /**
     * Returns all the jobs, registered in the Quartz Scheduler, grouped bu their corresponding job groups.
     *
     * @return Map <String , List<JobDescriptor>> with job group names as keys
     */
    def getAllJobs() {
        quartzScheduler.jobGroupNames.inject([:]) { acc, group -> acc[group] = getJobs(group) }
    }

    /**
     * Returns all the jobs, registered in the Quartz Scheduler, which belong to the specified group.
     *
     * @param group — the jobs group name
     * @return a list of corresponding JobDescriptor objects
     */
    def getJobs(String group) {
        quartzScheduler.getJobNames(group).collect { jobName ->
            JobDescriptor.build(quartzScheduler.getJobDetail(jobName, jobGroup), quartzScheduler)
        }
    }

    /**
     * Returns a list of all currently executing jobs.
     *
     * @return a List<JobExecutionContext>, containing all currently executing jobs.
     */
    def getRunningJobs() {
        quartzScheduler.getCurrentlyExecutingJobs()
    }

    def pauseJob(String group, String name) {
        quartzScheduler.pauseJob(name, group)
    }

    def resumeJob(String group, String name) {
        quartzScheduler.resumeJob(name, group)
    }

    def pauseTrigger(String group, String name) {
        quartzScheduler.pauseTrigger(name, group)
    }

    def resumeTrigger(String group, String name) {
        quartzScheduler.resumeTrigger(name, group)
    }

    def pauseTriggerGroup(String group) {
        quartzScheduler.pauseTriggerGroup(group)
    }

    def resumeTriggerGroup(String group) {
        quartzScheduler.resumeTriggerGroup(group)
    }

    def pauseJobGroup(String group) {
        quartzScheduler.pauseJobGroup(group)
    }

    def resumeJobGroup(String group) {
        quartzScheduler.resumeJobGroup(group)
    }

    def removeJob(String group, String name) {
        quartzScheduler.deleteJob(name, group)
    }

    def unscheduleJob(String group, String name) {
        quartzScheduler.unscheduleJob(name, group)
    }

    def interruptJob(String group, String name) {
        quartzScheduler.interrupt(name, group)
    }
}
