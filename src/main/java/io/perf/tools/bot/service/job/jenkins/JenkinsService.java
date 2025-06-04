package io.perf.tools.bot.service.job.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.JenkinsVersion;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import io.perf.tools.bot.model.config.JobDef;
import io.perf.tools.bot.model.config.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.job.JobExecutor;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@ApplicationScoped
public class JenkinsService implements JobExecutor {

    @ConfigProperty(name = "proxy.job.runner.jenkins.url")
    URI jenkinsUri;

    @Inject
    ConfigService configService;

    @Inject
    HttpClientBuilder jenkinsHttpClientBuilder;

    private JenkinsServer createJenkinsServer(ProjectConfig config) {
        // Create Jenkins client with the custom HttpClient
        JenkinsHttpClient client = new JenkinsHttpClient(jenkinsUri, jenkinsHttpClientBuilder, config.jobPlatformUser,
                config.jobPlatformApiKey);

        return new JenkinsServer(client);
    }

    @Override
    public boolean checkConnection(ProjectConfig config) {
        try (JenkinsServer jenkinsServer = createJenkinsServer(config)) {
            // TODO: this is not enough to validate the api key
            JenkinsVersion version = jenkinsServer.getVersion();
            Log.info("Connection with Jenkins " + version + " verified!");
            return true;
        } catch (Exception e) {
            Log.error("Unable to verify connection with Jenkins platform", e);
            return false;
        }
    }

    @Override
    public String buildJob(String repoFullName, String jobId, Map<String, String> params) throws IOException {
        Log.info("Building job " + jobId + " for " + repoFullName);
        ProjectConfig config = configService.getConfig(repoFullName);
        if (config == null) {
            throw new IllegalArgumentException("Config not found with `" + repoFullName + "`");
        }

        JobDef jobDef = config.jobs.get(jobId);
        try (JenkinsServer jenkinsServer = createJenkinsServer(config)) {
            JobWithDetails jenkinsJob = jenkinsServer.getJob(jobDef.platformJobId);

            QueueReference queueReference;
            // retrieve the next build number for this job
            int nextBuildNumber = jenkinsJob.getNextBuildNumber();
            if (jobDef.configurableParams.isEmpty()) {
                queueReference = jenkinsJob.build();
            } else {
                queueReference = jenkinsJob.build(params);
            }

            Log.debug("Job" + jobId + " queued at " + queueReference.getQueueItemUrlPart());
            return Integer.toString(nextBuildNumber);
        }
    }
}
