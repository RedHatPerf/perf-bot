package io.perf.tools.bot.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.perf.tools.bot.service.datastore.horreum.HorreumService;
import io.perf.tools.bot.service.datastore.horreum.util.ExperimentResultConverter;
import io.perf.tools.bot.service.datastore.horreum.util.LabelValueMapConverter;
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

    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    HorreumService horreumService;

    @Inject
    ExperimentResultConverter experimentResultConverter;

    @Inject
    LabelValueMapConverter labelValueMapConverter;

    @POST
    @ResponseStatus(204)
    @Produces(MediaType.APPLICATION_JSON)
    public void webhook(ObjectNode payload) {
        // when a new run is uploaded to Horreum we will check whether we have an existing "start benchmark" event in the queue
        // if so, we will get it and send back the results to the original pull request
        Log.info("Received webhook: " + payload.toString());
        String repoFullName = payload.get("data").get("info").get("repo_full_name").asText();
        int pullRequestNumber = payload.get("data").get("info").get("pull_request").asInt();
        String runId = payload.get("id").asText();

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
