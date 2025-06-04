package io.perf.tools.bot.service.datastore;

import io.perf.tools.bot.model.config.ProjectConfig;

public interface Datastore {

    /**
     * Checks whether the connection with the datastore is working
     * with the provided configuration
     *
     * @param config repository configuration
     */
    boolean checkConnection(ProjectConfig config);

    /**
     * Retrieve a specific run execution result
     *
     * @param repoFullName repository full name
     * @param jobId id of the job for which we want to get the run
     * @param runId run to compare
     * @return results as string
     */
    String getRun(String repoFullName, String jobId, String runId);


    /**
     * Compare the specified run against a pre-defined baseline
     *
     * @param repoFullName repository full name
     * @param jobId id of the job for which we want to get a comparison against baseline
     * @param runId run to compare
     * @return comparison results as string
     */
    String compare(String repoFullName, String jobId, String runId);
}
