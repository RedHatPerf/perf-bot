package io.perf.tools.bot.service;

import io.perf.tools.bot.action.Action;
import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResolver;
import io.perf.tools.bot.action.impl.internal.Comment;
import io.perf.tools.bot.action.impl.internal.Validate;
import io.perf.tools.bot.model.ProjectConfig;
import io.quarkiverse.githubapp.runtime.github.PayloadHelper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

/**
 * Service handling GitHub issue comment events for the performance bot.
 * <p>
 * It resolves commands from comments, validates them, executes the actions,
 * and posts the results as comments back to GitHub issues or pull requests.
 * </p>
 */
@ApplicationScoped
public class PerfBotService {

    public static final String ISSUE_COMMENT_CREATE_ACTION = "created";

    @Inject
    ActionResolver actionResolver;

    @Inject
    Validate validateAction;

    @Inject
    Comment commentAction;

    @Inject
    ConfigService configService;

    /**
     * Processes a GitHub issue comment event.
     * <p>
     * Extracts the command from the comment, loads the relevant project configuration,
     * validates, executes the command, and posts the result.
     * Ignores comments that are not newly created.
     * </p>
     *
     * @param issueComment the GitHub issue comment event payload
     */
    public void onGithubComment(GHEventPayload.IssueComment issueComment) {
        Action action = actionResolver.getActionFromComment(issueComment.getComment().getBody());
        ProjectConfig config = configService.getConfig(PayloadHelper.getRepository(issueComment).getFullName());

        // skip any issue comment action that is not "created", e.g., ignore comment deletion
        if (action != null && ISSUE_COMMENT_CREATE_ACTION.equalsIgnoreCase(issueComment.getAction())) {
            ActionContext<GHEventPayload.IssueComment> ctx = new ActionContext<>(issueComment, config);
            executeCommand(action, ctx);
        }
    }

    /**
     * Executes a chain of actions (validation, requested command, commenting).
     *
     * @param action the main action to execute
     * @param ctx the context of the action execution
     */
    private void executeCommand(Action action, ActionContext<?> ctx) {
        try {
            Action root = validateAction
                    .then(action) // the request command
                    .then(commentAction);
            root.execute(ctx);

            if (ActionContext.Status.isFailure(ctx.getStatus())) {
                Log.error("Something went wrong processing command: " + ctx);
            }
        } catch (Exception e) {
            Log.error("Some unexpected error occurred", e);
        }
    }
}