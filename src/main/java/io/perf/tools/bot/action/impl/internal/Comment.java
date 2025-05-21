package io.perf.tools.bot.action.impl.internal;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.exception.ActionExecutionException;
import io.perf.tools.bot.action.impl.BaseAction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

/**
 * An internal, special action that posts a comment to a GitHub issue or pull request
 * based on the current action context.
 * <p>
 * This action extends {@link BaseAction} and is typically used as a follow-up action to provide feedback
 * (success, failure, or diagnostics) by commenting on the original issue or pull request that triggered the bot.
 * </p>
 * <p>
 * If the context contains a non-blank message, the bot attempts to post it as a comment. If an error is present
 * and not of type {@link ActionExecutionException}, it is treated as an unexpected failure and logged accordingly.
 * </p>
 * <p>
 * If there is no message to post or the comment fails, the action context is marked as FAILED with the corresponding error.
 * </p>
 * <p>
 * The action name is {@value #NAME}, and it is marked as internal via the {@link #internal()} method, meaning it is not triggered
 * directly by user commands.
 * </p>
 */
@ApplicationScoped
public class Comment extends BaseAction {
    public static final String NAME = "comment";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean internal() {
        return true;
    }

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        Throwable error = ctx.getError();
        if (error != null && !(error instanceof ActionExecutionException)) {
            Log.error("Unexpected error", error);
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Unexpected error during comment action")
                    .setError(error);
        } else if (!ctx.getMessage().isBlank()) {
            // either success or ActionExecutionException issue
            try {
                ctx.getIssue().comment(ctx.getMessage());
            } catch (Exception e) {
                Log.error("Failed to comment issue", e);
                ctx.setStatus(ActionContext.Status.FAILED)
                        .setMessage("Failed to comment issue")
                        .setError(new ActionExecutionException("Failed to comment issue", e));
            }
        } else {
            // blank message
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("No comment found")
                    .setError(new ActionExecutionException("No comment found", null));
        }
    }

    @Override
    public void execute(ActionContext<?> ctx) throws IOException {
        if (ctx.getIssue() != null) {
            proceed(ctx);
        } else {
            Log.info("Executing comment action without any GitHub issue: " + ctx);
        }
    }
}
