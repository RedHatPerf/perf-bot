package io.perf.tools.bot.action.impl.internal;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.exception.EventNotSupportedException;
import io.perf.tools.bot.action.exception.ValidationException;
import io.perf.tools.bot.action.impl.BaseAction;
import io.quarkiverse.githubapp.runtime.github.PayloadHelper;
import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

/**
 * An internal action that validates whether the current issue comment event
 * is authorized and applicable for further processing by the bot.
 * <p>
 * This action extends {@link BaseAction} and performs a series of validation checks:
 * <ul>
 *   <li>Ensures the repository is configured.</li>
 *   <li>Checks that the event is a {@link GHEventPayload.IssueComment}.</li>
 *   <li>Verifies that the comment is made on a pull request.</li>
 *   <li>Confirms that the comment author is an authorized user.</li>
 * </ul>
 * </p>
 * <p>
 * If all checks pass, the action context is marked as SUCCESS. Otherwise, it is marked as FAILED
 * with an appropriate error message and exception type.
 * </p>
 * <p>
 * The action name is {@value #NAME}, and it is marked as internal via the {@link #internal()} method.
 * </p>
 */
@ApplicationScoped
public class Validate extends BaseAction {
    public static final String NAME = "validate";

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
        // fail fast if the repository is not configured
        if (ctx.getProjectConfig() == null) {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("No project config found for this repository, is it configured?")
                    .setError(new ValidationException(
                            "No project config found for " + PayloadHelper.getRepository(ctx.getPayload()).getFullName(),
                            null));
            return;
        }

        if (!(ctx.getPayload() instanceof GHEventPayload.IssueComment)) {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Event not supported")
                    .setError(new EventNotSupportedException("Event " + ctx.getPayload().getClass().getSimpleName() + " not supported for " + this.getClass()
                            .getSimpleName(), null));
        }

        if (ctx.getIssue() != null && !ctx.getIssue().isPullRequest()) {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Sorry! I can only run performance tests on PRs")
                    .setError(new ValidationException(
                            "Current issue is not a pull request " + ctx.getIssue().getUrl().toString(),
                            null));
        }

        String login = ctx.getPayload().getSender().getLogin();
        if (ctx.getProjectConfig().authorizedUsers.contains(login)) {
            ctx.setStatus(ActionContext.Status.SUCCESS)
                    .setMessage("User " + login + " is authorized")
                    .setError(null);
        } else {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("I am sorry " + login + ", you are not authorized to interact with perf-bot in this repository")
                    .setError(new ValidationException("User " + login + " is not authorized", null));
        }
    }
}
