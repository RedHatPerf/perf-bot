package io.perf.tools.bot.handler;

import io.perf.tools.bot.service.PerfBotService;
import io.quarkiverse.githubapp.event.IssueComment;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

/**
 * Handles incoming GitHub events and delegates them to the appropriate service layer.
 * <p>
 * This class currently listens for issue comment events and passes them to the {@link PerfBotService}
 * for further processing.
 * </p>
 * <p>
 * It is intended to act as a lightweight adapter between GitHub event payloads and the bot's business logic.
 * </p>
 *
 * @see PerfBotService
 * @see GHEventPayload.IssueComment
 */
public class GithubEventHandler {

    @Inject
    PerfBotService perfBotService;

    public void onComment(@IssueComment GHEventPayload.IssueComment issueComment) {
        perfBotService.onGithubComment(issueComment);
    }
}
