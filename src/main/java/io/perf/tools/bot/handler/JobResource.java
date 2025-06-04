package io.perf.tools.bot.handler;

import io.perf.tools.bot.model.JobStatus;
import io.perf.tools.bot.service.job.BuildStatus;
import io.perf.tools.bot.service.job.JobExecutor;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.kohsuke.github.GHIssue;

import java.io.IOException;

/**
 * REST resource to handle webhook callbacks from Job executor.
 * <p>
 * Receives notifications about job completion and post failures
 * back to the corresponding GitHub pull request.
 * </p>
 */
@Path("/job")
public class JobResource extends GitHubAwareResource {

    @Inject
    JobExecutor jobExecutor;

    /**
     * Handles incoming webhook payloads from the Job executor platform.
     * <p>
     * Validates the payload, retrieves the original repository and pull request, and
     * then it provides the results of the job back to the PR.
     * </p>
     * <p>
     * In this case the results is either the job has failed or unstable, skip posting
     * the message if the job has completed successfully
     * </p>
     *
     * @param payload the  {@link JobStatus} object
     */
    @POST
    @ResponseStatus(204)
    @Produces(MediaType.APPLICATION_JSON)
    public void webhook(JobStatus payload) {
        // expecting to get called by the job executor when the job is finished
        // regardless of the result
        Log.trace("Received job webhook: " + payload.toString());

        try {
            GHIssue issue = gitHubService.getInstallationClient(installationId).getRepository(payload.repoFullName)
                    .getIssue(payload.pullRequestNumber);

            BuildStatus resultStatus = jobExecutor.getJobStatus(payload.repoFullName, payload.jobId, payload.buildNumber);
            // TODO: do not post any message if the result is a success
            if (!BuildStatus.SUCCESS.equals(resultStatus)) {
                issue.comment(
                        "Job " + payload.jobId + "/" + payload.buildNumber + " completed with: " + resultStatus + "\nPlease check with the administrators.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
