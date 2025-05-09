package io.perf.tools.bot.service;

import io.perf.tools.bot.action.Action;
import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResolver;
import io.perf.tools.bot.action.ActionStatus;
import io.perf.tools.bot.action.impl.Comment;
import io.perf.tools.bot.action.impl.Validate;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

@ApplicationScoped
public class PullRequestService {

    @Inject
    ActionResolver actionResolver;

    @Inject
    Validate validateAction;

    @Inject
    Comment commentAction;

    public void onGithubComment(GHEventPayload.IssueComment issueComment) throws IOException {
        Action action = actionResolver.getActionFromComment(issueComment.getComment().getBody());
        if (action != null) {
            ActionContext ctx = new ActionContext(issueComment)
                    .process(validateAction)
                    .process(action)
                    .process(commentAction);

            if (ctx.getCurrentResult().getStatus() != null && !ActionStatus.SUCCESS.equals(
                    ctx.getCurrentResult().getStatus())) {
                // something went wrong
                Log.error("Something went wrong while processing comment " + issueComment.getComment()
                        .getBody() + ": " + ctx.getCurrentResult().getMessage());
            }
        }
    }
}