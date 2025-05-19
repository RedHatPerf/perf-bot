package io.perf.tools.bot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.perf.tools.bot.service.PullRequestService;
import io.quarkiverse.githubapp.runtime.github.GitHubService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.io.StringReader;

@ApplicationScoped
public class UMBHandler {

    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    PullRequestService pullRequestService;

    // TODo: Replace hard-coded topic
    @Incoming("Consumer.app-svc-perf.perf-bot-app.VirtualTopic.external.github.lampajr.webhook-umb-example")
    public void onComment(String payload) throws IOException {
        Log.debug("Received UMB:\n" + payload);

        GitHub github = gitHubService.getInstallationClient(installationId);
        GHEventPayload.IssueComment issueComment = github.parseEventPayload(new StringReader(payload), GHEventPayload.IssueComment.class);

        pullRequestService.onGithubComment(issueComment);
    }

}
