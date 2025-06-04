package io.perf.tools.bot.model;

/**
 * Represents the job completion object.
 * This class holds all necessary details to properly retrieve the
 * associated GitHub repository and pull request so that the bot
 * can update them with the success/failure of the job
 */
public class JobStatus {
    /**
     * Job identifier
     */
    public String jobId;

    /**
     * Identifier of the specific build of the job
     */
    public String buildNumber;

    /**
     * Repository full name in the form of
     * owner/repository
     */
    public String repoFullName;

    /**
     * Number of the pull request that triggered the job
     */
    public Integer pullRequestNumber;

    @Override
    public String toString() {
        return "JobStatus{" +
                "jobId='" + jobId + '\'' +
                ", buildNumber='" + buildNumber + '\'' +
                ", repoFullName='" + repoFullName + '\'' +
                ", pullRequestNumber=" + pullRequestNumber +
                '}';
    }
}
