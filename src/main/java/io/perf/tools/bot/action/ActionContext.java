package io.perf.tools.bot.action;

import io.perf.tools.bot.model.ProjectConfig;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;

/**
 * Holds contextual information shared and updated across a chain of {@link Action} instances.
 * <p>
 * This class encapsulates the GitHub event payload, project-specific configuration, and
 * mutable execution state such as status, error, and status messages.
 * </p>
 * <p>
 * Actions in the chain can read or modify the context as they process the event.
 * </p>
 * @param <P> the specific type of {@link org.kohsuke.github.GHEventPayload} associated with this context
 */
public class ActionContext<P extends GHEventPayload> {

    // input data
    private final P payload;
    private final GHIssue issue;
    private final ProjectConfig projectConfig;

    // current status
    private Status status;
    private String message;
    private Throwable error;

    public ActionContext(P payload, ProjectConfig projectConfig) {
        this.payload = payload;
        this.projectConfig = projectConfig;
        if (payload instanceof GHEventPayload.IssueComment issueComment) {
            this.issue = issueComment.getIssue();
        } else {
            this.issue = null;
        }
    }

    public P getPayload() {
        return payload;
    }

    public GHIssue getIssue() {
        return issue;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public Status getStatus() {
        return status;
    }

    public ActionContext<P> setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ActionContext<P> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Throwable getError() {
        return error;
    }

    public ActionContext<P> setError(Throwable error) {
        this.error = error;
        return this;
    }

    @Override
    public String toString() {
        return "ActionContext{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", error=" + error +
                '}';
    }

    /**
     * Represents the outcome of an {@link Action} execution.
     * <p>
     * This status is used to determine whether an action was executed successfully
     * or if it encountered a failure. It can influence whether subsequent actions
     * in a workflow are executed.
     */
    public enum Status {
        RUNNING,
        SUCCESS,
        FAILED;

        /**
         * Utility method to check if the given action is still running.
         *
         * @param status the status to check
         * @return {@code true} if the status is {@link #RUNNING}, {@code false} otherwise
         */
        public static boolean isRunning(Status status) {
            return status == RUNNING;
        }

        /**
         * Utility method to check if the given action has completed and its status
         * represents a success.
         *
         * @param status the status to check
         * @return {@code true} if the status is {@link #SUCCESS}, {@code false} otherwise
         */
        public static boolean isSuccess(Status status) {
            return status == SUCCESS;
        }

        /**
         * Utility method to check if the given action  has completed and its status
         * represent a failure.
         *
         * @param status the status to check
         * @return {@code true} if the status is {@code null} or {@link #FAILED}, {@code false} otherwise
         */
        public static boolean isFailure(Status status) {
            return status == FAILED;
        }
    }
}
