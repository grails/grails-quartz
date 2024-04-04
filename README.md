# Grails Quartz Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.grails.plugins/quartz.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.grails.plugins/quartz)
[![Java CI](https://github.com/grails/grails-quartz/actions/workflows/gradle.yml/badge.svg?event=push)](https://github.com/grails/grails-quartz/actions/workflows/gradle.yml)

## Documentation

[Latest documentation](https://grails.github.io/grails-quartz/latest/) and [snapshots](https://grails.github.io/grails-quartz/snapshot/) are available.

## Branches

| Branch  | Grails Version |
|---------|----------------|
| 1.x     | 2              |
| 2.0.x   | 3-5            |
| 3.0.x   | 6              |

## Using
### Quick start
To start using Quartz plugin just simply add
`implementation 'org.grails.plugins:quartz:{version}'` in your `build.gradle`.

>[!NOTE]
> __2.0.13 for Grails 3.3.*__\
> Properties changed to `static` from `def`.\
> For example: `def concurrent` will be now `static concurrent`.

### Scheduling Jobs
To create a new job run the `grails create-job` command and enter the name of the job. Grails will create a new job and place it in the `grails-app/jobs` directory:
```groovy
package com.mycompany.myapp

class MyJob {

    static triggers = {
        simple repeatInterval: 1000
    }

    void execute() {
        print "Job run!"
    }
}
```

The above example will call the `execute()` method every second.

### Scheduling configuration syntax

Currently, plugin supports three types of [triggers](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-02.html):
* **simple trigger** — executes once per defined interval (ex. "every 10 seconds");
* **cron trigger** — executes job with cron expression (ex. "at 8:00 am every Monday through Friday");
* **custom trigger** — your implementation of [Trigger](http://www.quartz-scheduler.org/api/2.3.0/org/quartz/Trigger.html) interface.

Multiple triggers per job are allowed.
```groovy
class MyJob {

    static triggers = {
        simple name: 'simpleTrigger', startDelay: 10000, repeatInterval: 30000, repeatCount: 10
        cron name: 'cronTrigger', startDelay: 10000, cronExpression: '0/6 * 15 * * ?'
        custom name: 'customTrigger', triggerClass: MyTriggerClass, myParam: myValue, myAnotherParam: myAnotherValue
    }

    void execute() {
        println "Job run!"
    }
}
```

With this configuration, job will be executed 11 times with 30 seconds interval with first run in 10 seconds after scheduler startup (simple trigger), also it'll be executed each 6 second during 15th hour (15:00:00, 15:00:06, 15:00:12, ... — this configured by cron trigger) and also it'll be executed each time your custom trigger will fire.

Three kinds of triggers are supported with the following parameters. The name field must be unique:
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
  * `triggerClass`  — your class which implements [CalendarIntervalTriggerImpl](http://www.quartz-scheduler.org/api/2.3.0/org/quartz/impl/triggers/CalendarIntervalTriggerImpl.html) impl;
  * any params needed by your trigger.

### Configuration plugin syntax

You can add the following properties to control persistence or not persistence:
* `quartz.pluginEnabled` - defaults to `true`, can disable plugin for test cases etc
* `quartz.jdbcStore` - `true` to enable database store, `false` to use RamStore (default: `true`)
* `quartz.autoStartup` - delays jobs until after bootstrap startup phase (default: `false`)
* `quartz.jdbcStoreDataSource` - jdbc data source alternate name
* `quartz.waitForJobsToCompleteOnShutdown` - wait for jobs to complete on shutdown (default: `true`)
* `quartz.exposeSchedulerInRepository` - expose Schedule in repository
* `quartz.scheduler.instanceName` - name of the scheduler to avoid conflicts between apps
* `quartz.purgeQuartzTablesOnStartup` - when jdbcStore set to `true` and this is `true`, clears out all quartz tables on startup
