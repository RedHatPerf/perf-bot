package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.Action;
import io.perf.tools.bot.action.ActionContext;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

/**
 * Abstract base class for {@link io.perf.tools.bot.action.Action} implementations.
 * <p>
 * This class provides default behavior for handling action execution flow and
 * determines whether an action should be executed based on the {@link io.perf.tools.bot.action.ActionContext} status.
 * Subclasses are expected to implement the {@link #proceed(io.perf.tools.bot.action.ActionContext)} method,
 * which contains the core logic of the action.
 */
public abstract class BaseAction implements Action {
    @ConfigProperty(name = "perf.bot.prompt")
    protected String prompt;

    /**
     * Returns the name of this action.
     * <p>
     * This name is typically used for identification and invocation purposes (e.g., matching
     * user input to a registered action).
     *
     * @return the name of the action
     */
    public abstract String getName();

    /**
     * Proceed executing the action, assuming that the {@link ActionContext} is not marked as {@code FAILED}.
     *
     * @param ctx the action execution context
     * @throws IOException if an I/O error occurs during execution
     */
    protected abstract void proceed(ActionContext<?> ctx) throws IOException;

    /**
     * Indicates whether this action is internal.
     * <p>
     * Internal actions are not exposed to users directly and are intended for internal
     * processing or infrastructure usage.
     *
     * @return {@code true} if the action is internal and should be hidden from users;
     *         {@code false} if the action is user-accessible.
     */
    public boolean internal() {
        return false;
    }

    /**
     * Proceed executing the action, but only if the current context result is not marked as {@code FAILED}.
     * <p>
     * If the context is already in a failed state, the action is skipped.
     * Otherwise, the {@link #proceed(io.perf.tools.bot.action.ActionContext)} method is invoked to perform the action logic.
     *
     * @param ctx the action execution context
     * @throws IOException if an I/O error occurs during execution
     */
    @Override
    public void execute(ActionContext<?> ctx) throws IOException {
        if (ctx != null && !ActionContext.Status.isFailure(ctx.getStatus())) {
            proceed(ctx);
        } else {
            Log.warn("Skipping action " + getClass().getSimpleName() + " because previous status is failure");
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
