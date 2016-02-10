package grails.plugins.quartz.cleanup

import groovy.sql.Sql
import groovy.util.logging.Slf4j;

import javax.annotation.PostConstruct;

/**
 * Contributed by Rocketmiles
 * This class can purge all of the quartz tables on startup if you set the config flag quartz.purgeQuartzTablesOnStartup = true
 * Mostly used for testing or development purposes. It is not recommended you use this for production as you can
 * miss missfire recoveries, persisted jobs/triggers, etc.
 */

@Slf4j
public class JdbcCleanup {

    def dataSource
    def grailsApplication

    @PostConstruct
    void init() {

        log.info "[quartz-plugin] Purging Quartz tables...."

        def queries = []
        queries.add("DELETE FROM QRTZ_FIRED_TRIGGERS")
        queries.add("DELETE FROM QRTZ_PAUSED_TRIGGER_GRPS")
        queries.add("DELETE FROM QRTZ_SCHEDULER_STATE")
        queries.add("DELETE FROM QRTZ_LOCKS")
        queries.add("DELETE FROM QRTZ_SIMPLE_TRIGGERS")
        queries.add("DELETE FROM QRTZ_SIMPROP_TRIGGERS")
        queries.add("DELETE FROM QRTZ_CRON_TRIGGERS")
        queries.add("DELETE FROM QRTZ_BLOB_TRIGGERS")
        queries.add("DELETE FROM QRTZ_TRIGGERS")
        queries.add("DELETE FROM QRTZ_JOB_DETAILS")
        queries.add("DELETE FROM QRTZ_CALENDARS")

        def sql = new Sql(dataSource)
        queries.each { query ->
                log.info("Executing " + query)
            sql.execute(query)
        }


    }
}
