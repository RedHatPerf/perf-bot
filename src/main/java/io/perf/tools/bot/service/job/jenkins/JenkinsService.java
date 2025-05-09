package io.perf.tools.bot.service.job.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import io.perf.tools.bot.model.JobDef;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.job.JobRunner;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class JenkinsService implements JobRunner {

    @Inject
    ConfigService configService;

    @Inject
    JenkinsServer jenkinsServer;

    @Override
    public void buildJob(String repoFullName, String jobId, Map<String, String> params) {
        Log.info("Building job " + jobId + " for " + repoFullName);
        ProjectConfig config = configService.getConfig(repoFullName);
        JobDef jobDef = config.jobs.get(jobId);

        try {
            JobWithDetails jenkinsJob = jenkinsServer.getJob(jobDef.jenkinsJob);

            QueueReference queueReference;
            if (params.isEmpty()) {
                queueReference = jenkinsJob.build(Map.of());
            } else {
                queueReference = jenkinsJob.build(params);
            }
            Log.info("Building job " + jobDef.name + " " + queueReference.getQueueItemUrlPart());
        } catch (Exception e) {
            Log.error("Something went wrong", e);
            throw new RuntimeException(e);
        }
        Log.info("build completed");
    }
}
