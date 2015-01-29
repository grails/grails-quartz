description("Creates a new Quartz scheduled job") {
    usage "grails create-job [JOB NAME]"
    argument name:'Job Name', description:"The name of the job"
}

model = model( args[0] )
render  template:"Job.groovy",
        destination: file( "grails-app/jobs/$model.packagePath/${model.simpleName}Job.groovy"),
        model: model
