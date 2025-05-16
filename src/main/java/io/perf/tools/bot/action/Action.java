package io.perf.tools.bot.action;

import io.perf.tools.bot.action.impl.Help;

import java.io.IOException;

/**
 * Generic command that users can use, either through the GitHub pull request
 * or through other supported entry points
 */
public interface Action {

    /**
     * Whether this is an internal actions, and therefore not exposed to the users
     * @return true if that is internal, false otherwise
     */
    boolean internal();

    ActionContext perform(ActionContext context) throws IOException;

    String getName();
}
