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

import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class JenkinsService implements JobRunner {

    @Inject
    ConfigService configService;

    @Inject
    JenkinsServer jenkinsServer;

    @Override
    public String buildJob(String configId, String jobId, Map<String, String> params) throws IOException {
        Log.info("Building job " + jobId + " for " + configId);
        ProjectConfig config = configService.getConfig(configId);
        if (config == null) {
            throw new IllegalArgumentException("Config not found with `" + configId + "`");
        }

        JobDef jobDef = config.jobs.get(jobId);
        JobWithDetails jenkinsJob = jenkinsServer.getJob(jobDef.jenkinsJob);

        QueueReference queueReference;

        if (jobDef.configurableParams.isEmpty()) {
            queueReference = jenkinsJob.build();
        } else {
            queueReference = jenkinsJob.build(params);
        }

        return queueReference.getQueueItemUrlPart();
    }
}
