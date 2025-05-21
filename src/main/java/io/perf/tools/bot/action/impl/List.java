package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.quarkiverse.githubapp.runtime.github.PayloadHelper;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Arrays;

/**
 * An action that lists all available jobs configured for the current repository.
 * <p>
 * This action extends {@link BaseAction} and provides functionality to retrieve and display
 * the names of jobs configured in the project's configuration. If the repository configuration
 * is missing, it marks the action as failed with an appropriate error message.
 * </p>
 * <p>
 * The action name is {@value #NAME}.
 * </p>
 */
@ApplicationScoped
public class List extends BaseAction {

    public static final String NAME = "list";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        if (ctx.getProjectConfig() == null) {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Current repository is not configured")
                    .setError(new IllegalStateException("Current repository is not configured"));
        } else {
            String msg = "Available jobs for " + PayloadHelper.getRepository(ctx.getPayload())
                    .getFullName() + ": " + Arrays.toString(ctx.getProjectConfig().jobs.keySet().toArray());
            ctx.setStatus(ActionContext.Status.SUCCESS)
                    .setMessage(msg)
                    .setError(null);
        }
    }
}
