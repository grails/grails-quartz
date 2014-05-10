# Grails Quartz Plugin 
![](http://stillmaintained.com/nebolsin/grails-quartz.png)[![Build Status](https://travis-ci.org/grails-plugins/grails-quartz.svg)](https://travis-ci.org/grails-plugins/grails-quartz)

**Full documentation can be found [here](http://grails-plugins.github.io/grails-quartz/)**

This plugin has been updated to run on Quartz 2.2.x and no longer runs on Quartz 1.8.x. The newer
version of Quartz will also allow it to be used With Terrcotta 3.6 or later (I tested on 3.7) - Ryan

This is different than the quartz2 plugin others maintain, because it does not use JobDetailsImpl, that one will
not work with Terracotta due to [bug](https://jira.terracotta.org/jira/browse/QTZ-310). This plugin was the last holdback to allow a full Terracotta 3.7, Tomcat 7, Quartz 2.1.x, and Ehcache and Hibernate Cache solution all working at once!

Quartz plugin allows your Grails application to schedule jobs to be executed using a specified interval or cron
expression. The underlying system uses the [Quartz Enterprise Job Scheduler](http://www.quartz-scheduler.org/)
configured via [Spring](http://projects.spring.io/spring-framework/), but is made simpler by the coding by convention paradigm.

## Installation

To install the latest stable version of the plugin add this to your `BuildConfig.groovy` in the plugins section (be sure to use the latest version):
```groovy
compile ':quartz:1.0-RC11'
```

## Using
### Scheduling Jobs
To create a new job run the `grails create-job` command and enter the name of the job. Grails will create a new job and place it in the `grails-app/jobs` directory:
```groovy
class MyJob {
    static triggers = {
        simple repeatInterval: 1000
    }

    def execute(){
        print "Job run!"
    }
}
```
The above example will call the 'execute' method every second.

### Scheduling configuration syntax
Currently plugin supports three types of [triggers](http://quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-02):
* **simple trigger** — executes once per defined interval (ex. "every 10 seconds");
* **cron trigger** — executes job with cron expression (ex. "at 8:00 am every Monday through Friday");
* **custom trigger** — your implementation of [Trigger](http://www.quartz-scheduler.org/api/2.2.0/org/quartz/Trigger.html) interface.

Multiple triggers per job are allowed.
```groovy
class MyJob {
    static triggers = {
        simple name:'simpleTrigger', startDelay:10000, repeatInterval: 30000, repeatCount: 10
        cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
        custom name:'customTrigger', triggerClass:MyTriggerClass, myParam:myValue, myAnotherParam:myAnotherValue
    }

    def execute() {
        println "Job run!"
    }
  }
```

With this configuration job will be executed 11 times with 30 seconds interval with first run in 10 seconds after
scheduler startup (simple trigger), also it'll be executed each 6 second during 15th hour
(15:00:00, 15:00:06, 15:00:12, ... — this configured by cron trigger) and also it'll be executed each time your custom
trigger will fire.

Three kinds of triggers are supported with the following parameters:
* `simple`:
  * `name` — the name that identifies the trigger;
  * `startDelay` — delay (in milliseconds) between scheduler startup and first job's execution;
  * `repeatInterval` — timeout (in milliseconds) between consecutive job's executions;
  * `repeatCount` — trigger will fire job execution `(1 + repeatCount)` times and stop after that (specify `0`  here to have one-shot job or `-1` to repeat job executions indefinitely);
* `cron`:
  * `name` — the name that identifies the trigger;
  * `startDelay` — delay (in milliseconds) between scheduler startup and first job's execution;
  * `cronExpression` — [cron expression](http://www.quartz-scheduler.org/api/2.2.0/org/quartz/CronExpression.html)
* `custom`:
  * `triggerClass`  — your class which implements [CalendarIntervalTriggerImpl](http://www.quartz-scheduler.org/api/2.2.0/org/quartz/impl/triggers/CalendarIntervalTriggerImpl.html) impl;
  * any params needed by your trigger.
