package quartz

import grails.codegen.model.Model
import grails.dev.commands.GrailsApplicationCommand

class CreateJobCommand implements GrailsApplicationCommand {

    String getName() {
        return "create-job"
    }


    String getDescription() {
        return "Creates a new Quartz scheduled job"
    }


    boolean handle() {
        String classNameFull = args?.size() >= 1 ? args.first() : 'My'
        Model modelObject = model(trimTrailingJobFromJobName(classNameFull))
        if (!modelObject.packagePath) {
            String defaultPackage = applicationContext.getBean('application').class.getPackage().name
            String nameClasseWithDefaultPackage = "${defaultPackage}.$classNameFull".toString()
            modelObject = model(trimTrailingJobFromJobName(nameClasseWithDefaultPackage))


        }

        String pathDestinationFile = "grails-app/jobs/$modelObject.packagePath/${trimTrailingJobFromJobName(modelObject.simpleName)}Job.groovy"
        File fileDestination = file(pathDestinationFile)
        render template: "Job.groovy",
                destination: fileDestination,
                model: modelObject
        return true
    }

    /**
     * if 'Job' already exists in the end of JobName, then remove it from jobName.
     */
    private String trimTrailingJobFromJobName(String jobName) {
        if (jobName.endsWith("Job")) {
            return jobName.substring(0, jobName.length() - 3)
        }
        return jobName
    }

}
