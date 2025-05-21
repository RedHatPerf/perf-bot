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
 * An action that retrieves the results of a previously executed job run.
 * <p>
 * This action extends {@link BaseAction} and is triggered by {@link GHEventPayload.IssueComment} events.
 * It extracts the run ID from the comment (or defaults to the latest run) and queries the {@link Datastore}
 * for the corresponding results.
 * </p>
 * <p>
 * If the run results are successfully retrieved, the action context is marked as SUCCESS and
 * a formatted message with the results is returned. Otherwise, it is marked as FAILED with an error.
 * </p>
 * <p>
 * The action name is {@value #NAME}.
 * </p>
 */
@ApplicationScoped
public class Get extends BaseAction {
    public static final String NAME = "get";

    @Inject
    Datastore datastore;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        if (ctx.getPayload() instanceof GHEventPayload.IssueComment issueComment) {
            //0: prompt; 1: cmd; [2: run id];
            String[] comment = issueComment.getComment().getBody().split(" ");
            String runId = comment.length > 2 ? comment[2] : LATEST_RUN;

            try {
                String msg = "## Label values for run " + runId + "\n\n" + datastore.getRun(ctx.getProjectConfig().id, runId);
                ctx.setStatus(ActionContext.Status.SUCCESS)
                        .setMessage(msg)
                        .setError(null);
            } catch (Exception e) {
                Log.error("Failed to get run", e);
                ctx.setStatus(ActionContext.Status.FAILED)
                        .setMessage("Failed to get run execution from result store")
                        .setError(new ActionExecutionException("Failed to get run execution from result store", e));
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
