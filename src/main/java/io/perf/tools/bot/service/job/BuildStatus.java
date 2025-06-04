package io.perf.tools.bot.service.job;

import com.offbytwo.jenkins.model.BuildResult;

/**
 * Represent the status of a specific job's build
 * Inspired by the Jenkins build status (see {@link com.offbytwo.jenkins.model.BuildResult})
 */
public enum BuildStatus {
    FAILURE,
    UNSTABLE,
    REBUILDING,
    BUILDING,
    /**
     * This means a job was already running and has been aborted.
     */
    ABORTED,
    SUCCESS,
    UNKNOWN,
    /**
     * This is returned if a job has never been built.
     */
    NOT_BUILT,
    /**
     * This will be the result of a job in cases where it has been cancelled
     * during the time in the queue.
     */
    CANCELLED;

    public static BuildStatus fromJenkins(BuildResult jenkinsBuildResult) {
        if (jenkinsBuildResult == null) {
            return BuildStatus.UNKNOWN;
        }

        return switch (jenkinsBuildResult) {
            case SUCCESS -> BuildStatus.SUCCESS;
            case CANCELLED -> BuildStatus.CANCELLED;
            case FAILURE -> BuildStatus.FAILURE;
            case ABORTED -> BuildStatus.ABORTED;
            case UNSTABLE -> BuildStatus.UNSTABLE;
            case REBUILDING -> BuildStatus.REBUILDING;
            case BUILDING -> BuildStatus.BUILDING;
            default -> BuildStatus.UNKNOWN;
        };
    }
}
