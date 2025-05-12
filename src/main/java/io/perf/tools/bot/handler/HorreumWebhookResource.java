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

import java.io.IOException;

@Path("/horreum")
public class HorreumWebhookResource {

    private static final String REPO_FULL_NAME_LABEL_VALUE = "pb.repo_full_name";
    private static final String PULL_REQUEST_NUMBER_LABEL_VALUE = "pb.pull_request_number";

    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    HorreumService horreumService;

    @Inject
    ConfigService configService;

    @POST
    @ResponseStatus(204)
    @Produces(MediaType.APPLICATION_JSON)
    // TODO: can we use Run object here?
    public void webhook(ObjectNode payload) {
        // when a new run is uploaded to Horreum we will check whether we have an existing "start benchmark" event in the queue
        // if so, we will get it and send back the results to the original pull request
        Log.info("Received webhook: " + payload.toString());

        // use this to check whether we have a configuration for that test id, and retrieve the repo full name
        String horreumTestId = payload.get("testid").asText();
        String runId = payload.get("id").asText();
        ProjectConfig config = configService.getConfigByTestId(horreumTestId);
        if (config == null) {
            Log.warn("Config not found for Horreum test id: " + horreumTestId);
            return;
        }
        // the id coincides with the repo full name
        String repoFullName = config.id;
        // fetch the Horreum run labelValues limiting the values to the pull request number, repo full name
        // TODO: we could filter and include only those labels we are interested in
        LabelValueMap labelValueMap = horreumService.getRun(config, runId);
        String runRepoFullName = labelValueMap.get(REPO_FULL_NAME_LABEL_VALUE).asText();
        int pullRequestNumber = labelValueMap.get(PULL_REQUEST_NUMBER_LABEL_VALUE).asInt();

        if (!repoFullName.equals(runRepoFullName)) {
            Log.error("Configured repository "  + repoFullName + " does not match Run repo full name: " + runRepoFullName);
            throw new IllegalArgumentException("Configured repo full name does not match uploaded Run repo full name");
        }

        // check the two repo full names match each other
        // if PR number is null, skip this as it means it is not a pull request that triggered the upload

        // TODO: define two new labels: repo full name and pull request number

        StringBuilder comment = new StringBuilder();
        try {
            GHIssue issue = gitHubService.getInstallationClient(installationId).getRepository(repoFullName)
                    .getIssue(pullRequestNumber);

            comment.append("## Job results ").append(runId).append("\n");

            comment.append("### Label values").append("\n\n")
                    .append(horreumService.getRun(repoFullName, runId)).append("\n\n");

            comment.append("### Baseline comparison").append("\n\n")
                    .append(horreumService.compare(repoFullName, runId));

            issue.comment(comment.toString());
        } catch (IOException e) {
            Log.error("Error getting issue for " + repoFullName + " - " + pullRequestNumber, e);
            throw new RuntimeException(e);
        }
    }

}
