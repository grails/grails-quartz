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

import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher

/**
 * Simplifies interaction with the Quartz Scheduler.
 *
 * @author Marco Mornati (mmornati@byte-code.com)
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 *
 * @since 0.4
 */
class JobManagerService {

    static transactional = false

    Scheduler quartzScheduler

    /**
     * All jobs registered in the Quartz Scheduler, grouped by their corresponding job groups.
     *
     * @return the descriptors
     */
    Map<String, List<JobDescriptor>> getAllJobs() {
        quartzScheduler.jobGroupNames.collectEntries([:]) { group -> [(group):getJobs(group)]}
    }

    /**
     * All jobs registered in the Quartz Scheduler which belong to the specified group.
     *
     * @param group the jobs group name
     * @return the descriptors
     */
    List<JobDescriptor> getJobs(String group) {
        List<JobDescriptor> list = []
        quartzScheduler.getJobKeys(GroupMatcher.groupEquals(group)).each { jobKey ->
            def jobDetail = quartzScheduler.getJobDetail(jobKey)
            if (jobDetail != null) {
                list.add(JobDescriptor.build(jobDetail, quartzScheduler))
            }
        }
        return list
    }

    /**
     * All currently executing jobs.
     *
     * @return the contexts
     */
    List<JobExecutionContext> getRunningJobs() {
        quartzScheduler.getCurrentlyExecutingJobs()
    }

    void pauseJob(String group, String name) {
        quartzScheduler.pauseJob(new JobKey(name, group))
    }

    void resumeJob(String group, String name) {
        quartzScheduler.resumeJob(new JobKey(name, group))
    }

    void pauseTrigger(String group, String name) {
        quartzScheduler.pauseTrigger(new TriggerKey(name, group))
    }

    void resumeTrigger(String group, String name) {
        quartzScheduler.resumeTrigger(new TriggerKey(name, group))
    }

    void pauseTriggerGroup(String group) {
        quartzScheduler.pauseTriggers(GroupMatcher.groupEquals(group))
    }

    void resumeTriggerGroup(String group) {
        quartzScheduler.resumeTriggers(GroupMatcher.groupEquals(group))
    }

    void pauseJobGroup(String group) {
        quartzScheduler.pauseJobs(GroupMatcher.groupEquals(group))
    }

    void resumeJobGroup(String group) {
        quartzScheduler.resumeJobs(GroupMatcher.groupEquals(group))
    }

    void pauseAll() {
        quartzScheduler.pauseAll()
    }

    void resumeAll() {
        quartzScheduler.resumeAll()
    }

    boolean removeJob(String group, String name) {
        quartzScheduler.deleteJob(new JobKey(name, group))
    }

    boolean unscheduleJob(String group, String name) {
        quartzScheduler.unscheduleJobs(quartzScheduler.getTriggersOfJob(new JobKey(name, group))*.key)
    }

    boolean interruptJob(String group, String name) {
        quartzScheduler.interrupt(new JobKey(name, group))
    }
}
