package io.perf.tools.bot.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hyperfoil.tools.horreum.api.data.LabelValueMap;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.datastore.horreum.HorreumService;
import io.quarkiverse.githubapp.runtime.github.GitHubService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.kohsuke.github.GHIssue;

/**
 * REST resource to handle webhook callbacks from Horreum.
 * <p>
 * Receives notifications about new benchmark runs and posts results
 * back to the corresponding GitHub pull request.
 * </p>
 */
@Path("/horreum")
public class HorreumResource {

    private static final String REPO_FULL_NAME_LABEL_VALUE = "pb.repo_full_name";
    private static final String PULL_REQUEST_NUMBER_LABEL_VALUE = "pb.pull_request_number";
    private static final String JOB_ID_LABEL_VALUE = "pb.job_id";

    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    HorreumService horreumService;

    @Inject
    ConfigService configService;

    /**
     * Handles incoming webhook payloads from Horreum.
     * <p>
     * Validates the payload, fetches benchmark results, compares baselines,
     * and posts a comment on the related GitHub pull request.
     * </p>
     *
     * @param payload the JSON payload sent by Horreum webhook
     * @throws InterruptedException if the thread sleep is interrupted
     */
    @POST
    @ResponseStatus(204)
    @Produces(MediaType.APPLICATION_JSON)
    // TODO: instead of relying on Horreum webhook we could simply let Jenkins call this endpoint with some required information
    // e.g., job result, horreum run id, pull request number and repo full name
    public void webhook(ObjectNode payload) throws InterruptedException {
        // when a new run is uploaded to Horreum we will check whether we have an existing "start benchmark" event in the queue
        // if so, we will get it and send back the results to the original pull request
        Log.trace("Received webhook: " + payload.toString());

        // use this to check whether we have a configuration for that test id, and retrieve the repo full name
        String horreumTestId = payload.get("testid").asText();
        String runId = payload.get("id").asText();
        try {
            ProjectConfig config = configService.getConfigByTestId(horreumTestId);
            if (config == null) {
                Log.warn("Config not found for Horreum test id: " + horreumTestId);
                return;
            }
            String repoFullName = config.repoFullName;

            // FIXME: atm there is no guarantee that when a Run is upload the label values are immediately available
            // creating label values is async and could take time - we should not rely on this
            // TODO: we should probably add a new event in Horreum to subscribe to
            Thread.sleep(2000);
            // TODO: fetch the Horreum run labelValues limiting the values to the pull request number, repo full name and job id
            LabelValueMap labelValueMap = horreumService.getRun(config, runId);
            String runRepoFullName = labelValueMap.get(REPO_FULL_NAME_LABEL_VALUE).asText();
            int pullRequestNumber = labelValueMap.get(PULL_REQUEST_NUMBER_LABEL_VALUE).asInt();
            String jobId = labelValueMap.get(JOB_ID_LABEL_VALUE).asText();

            if (!repoFullName.equals(runRepoFullName)) {
                Log.error("Configured repository " + repoFullName + " does not match Run repo full name: " + runRepoFullName);
                throw new IllegalArgumentException("Configured repo full name does not match uploaded Run repo full name");
            }

            StringBuilder comment = new StringBuilder();
            GHIssue issue = gitHubService.getInstallationClient(installationId).getRepository(repoFullName)
                    .getIssue(pullRequestNumber);

            comment.append("## (").append(jobId).append(") Results of run ").append(runId).append("\n");

            comment.append(horreumService.getRun(repoFullName, jobId, runId)).append("\n");

            comment.append("### Experiments").append("\n\n")
                    .append(horreumService.compare(repoFullName, jobId, runId));

            issue.comment(comment.toString());
        } catch (Exception e) {
            Log.error("Error processing Horreum webhook event for test " + horreumTestId + " and run id " + runId, e);
            throw new RuntimeException(e);
        }
    }

}
