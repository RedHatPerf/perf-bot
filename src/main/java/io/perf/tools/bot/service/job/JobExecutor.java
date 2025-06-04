package io.perf.tools.bot.service.job;

import io.perf.tools.bot.model.config.ProjectConfig;

import java.io.IOException;
import java.util.Map;

public interface JobExecutor {

    /**
     * Checks whether the connection with the datastore is working
     * with the provided configuration
     *
     * @param config repository configuration
     */
    boolean checkConnection(ProjectConfig config);

    /**
     *
     * @param repoFullName repository full name
     * @param jobId job identifier
     * @param params additional job parameters
     * @return the triggered job location
     * @throws IOException if something goes wrong
     */
    String buildJob(String repoFullName, String jobId, Map<String, String> params) throws IOException;

    /**
     * Retrieves the job's build status
     *
     * @param repoFullName repository full name
     * @param jobId identifier of the job
     * @param buildNumber number of the build
     * @return the build status
     * @throws IOException is something goes wrong
     */
    BuildStatus getJobStatus(String repoFullName, String jobId, String buildNumber) throws IOException;
}
