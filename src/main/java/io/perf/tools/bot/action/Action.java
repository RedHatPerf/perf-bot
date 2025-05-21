package io.perf.tools.bot.action;

import java.io.IOException;

/**
 * Represents an action that operates on a shared {@link ActionContext}.
 * <p>
 * This is a functional interface with a single abstract method {@link #execute(ActionContext)}.
 * Actions can be chained together using the {@link #then(Action)} method to form a sequence
 * where each action receives the updated context from the previous one.
 */
@FunctionalInterface
public interface Action {

    /**
     * Executes this action using the given context.
     * Implementations typically modify or read data from the context.
     *
     * @param ctx the shared context passed through the action chain
     */
    void execute(ActionContext<?> ctx) throws IOException;

    /**
     * Returns a composed {@code Action} that performs, in sequence,
     * this action followed by the {@code next} action.
     * <p>
     * When the returned action is executed, it will first invoke this action's
     * {@code execute} method, then the {@code next} action's {@code execute} method,
     * passing the same shared context instance.
     *
     * @param next the action to execute after this action
     * @return a composed {@code Action} representing the sequence of this and the next action
     */
    default Action then(Action next) {
        return (ctx) -> {
            this.execute(ctx);
            next.execute(ctx);
        };
    }
}
