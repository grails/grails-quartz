/*
 * Copyright 2015 the original author or authors.
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

import grails.core.GrailsApplication
import grails.util.Holders
import grails.web.api.WebAttributes
import groovy.transform.CompileStatic
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.Scheduler

@CompileStatic
trait QuartzJob implements WebAttributes {

    static triggerNow(Map params = null) {
        def applicationContext = Holders.applicationContext
        def quartzScheduler = applicationContext.getBean('quartzScheduler', Scheduler)
        def grailsApplication = applicationContext.getBean('grailsApplication', GrailsApplication)
        GrailsJobClass jobArtefact = (GrailsJobClass)grailsApplication.getArtefact(DefaultGrailsJobClass.JOB, this.getName())
        quartzScheduler.triggerJob(new JobKey(this.getName(), jobArtefact.group), params ? new JobDataMap(params) : null)
    }
}
