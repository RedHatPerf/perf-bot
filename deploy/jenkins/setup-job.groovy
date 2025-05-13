jobScript = new File('/var/jenkins_home/job.Jenkinsfile').getText("UTF-8")
pipelineJob("getting-started") {
    description()
    keepDependencies(false)
    definition {
        cps {
            script(jobScript)
            sandbox(true)
        }
    }
}
