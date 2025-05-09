package io.perf.tools.bot.handler;

import io.perf.tools.bot.service.PullRequestService;
import io.quarkiverse.githubapp.event.IssueComment;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

public class GithubEventHandler {

    @Inject
    PullRequestService pullRequestService;

    public void onComment(@IssueComment GHEventPayload.IssueComment issueComment) throws IOException {
        pullRequestService.onGithubComment(issueComment);
    }
}
