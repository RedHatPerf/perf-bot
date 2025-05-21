package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.exception.ActionExecutionException;
import io.perf.tools.bot.action.exception.EventNotSupportedException;
import io.perf.tools.bot.service.datastore.Datastore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

import static io.perf.tools.bot.service.datastore.horreum.HorreumService.LATEST_RUN;

/**
 * An action that compares a specific job run against the configured baseline.
 * <p>
 * This action extends {@link BaseAction} and is triggered by {@link GHEventPayload.IssueComment} events.
 * It extracts the run ID from the issue comment (or defaults to the latest run), then uses the
 * {@link Datastore} to perform a comparison against the baseline data configured for the project.
 * </p>
 * <p>
 * If the comparison is successful, the action context is marked as SUCCESS and includes a formatted
 * comparison summary. If any error occurs, the action is marked as FAILED and includes the exception details.
 * </p>
 * <p>
 * The action name is {@value #NAME}.
 * </p>
 */
@ApplicationScoped
public class Compare extends BaseAction {
    public static final String NAME = "compare";

    @Inject
    Datastore datastore;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        if (ctx.getPayload() instanceof GHEventPayload.IssueComment issueComment) {
            //0: prompt; 1: cmd; 2: run id;
            String[] comment = issueComment.getComment().getBody().split(" ");
            String runId = comment.length > 2 ? comment[2] : LATEST_RUN;

            try {
                String msg = "## Comparison for " + runId + " against baseline" + "\n\n" + datastore.compare(
                        ctx.getProjectConfig().id, runId);
                ctx.setStatus(ActionContext.Status.SUCCESS)
                        .setMessage(msg)
                        .setError(null);
            } catch (Exception e) {
                Log.error("Failed to compare run", e);
                ctx.setStatus(ActionContext.Status.FAILED)
                        .setMessage("Failed to compare execution run")
                        .setError(new ActionExecutionException("Failed to compare execution run", e));
            }
        } else {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Event not supported")
                    .setError(new EventNotSupportedException(
                            "Event " + ctx.getPayload().getClass().getSimpleName() + " not supported for " + this.getClass()
                                    .getSimpleName(), null));
        }
    }
}
