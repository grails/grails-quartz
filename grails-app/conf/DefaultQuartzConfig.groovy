
quartz {
    autoStartup = true
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true

    props {
        scheduler.skipUpdateCheck = true
    }
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
}
