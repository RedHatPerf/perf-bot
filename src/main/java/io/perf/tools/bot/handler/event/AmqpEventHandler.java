package io.perf.tools.bot.handler.event;

import io.perf.tools.bot.service.PerfBotService;
import io.quarkiverse.githubapp.runtime.github.GitHubService;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.io.StringReader;

/**
 * Handler for receiving GitHub webhook events over AMQP channel, e.g., UMB (Unified Message Bus).
 * <p>
 * This class listens to a specific AMQ topic for GitHub issue comment events, deserializes the payload,
 * and forwards it to the {@link PerfBotService} for processing.
 * </p>
 * <p>
 * The GitHub API client is authenticated using a pre-configured GitHub App installation ID.
 * </p>
 *
 * @see PerfBotService
 * @see GitHubService
 */
@ApplicationScoped
public class AmqpEventHandler {

    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    PerfBotService perfBotService;

    // TODO: Replace hard-coded topic
    @Incoming("amqp-channel")
    public void onComment(String payload) throws IOException {
        Log.debug("Received AMQP message:\n" + payload);

        GitHub github = gitHubService.getInstallationClient(installationId);
        GHEventPayload.IssueComment issueComment = github.parseEventPayload(new StringReader(payload),
                GHEventPayload.IssueComment.class);

        perfBotService.onGithubComment(issueComment);
    }

}
