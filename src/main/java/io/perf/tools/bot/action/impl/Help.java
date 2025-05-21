package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

/**
 * An action that provides usage instructions for interacting with the bot.
 * <p>
 * This action extends {@link BaseAction} and returns a help message detailing available commands
 * such as {@code help}, {@code list}, {@code run}, {@code get}, and {@code compare}, along with example usages.
 * </p>
 * <p>
 * It is typically invoked when a user issues the {@code /perf-bot help} command in a supported context.
 * </p>
 * <p>
 * The action name is {@value #NAME}.
 * </p>
 */
@ApplicationScoped
public class Help extends BaseAction {

    public static final String NAME = "help";

    @Override
    public String getName() {
        return NAME;
    }

    private static final String PROMPT = "/perf-bot";
    private static final String HELP_MSG = "**Usage**: `" + PROMPT + " COMMAND [ARGS]...`\n\n" +
            "**Commands**:\n" +
            " * `help`            Show this help message.\n" +
            " * `list`            List all available jobs for this repository.\n" +
            " * `run JOB`         Trigger a remote job based on the repository configuration.\n" +
            " * `get RUN`         Get the results of a specific execution run.\n" +
            " * `compare RUN`     Compare a run against the configured baseline.\n" +
            "\n" +
            "**Examples**:\n" +
            " * `" + PROMPT + " run test`\n" +
            " * `" + PROMPT + " get 1`\n" +
            " * `" + PROMPT + " compare 5`\n";

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        ctx.setMessage(HELP_MSG).setStatus(ActionContext.Status.SUCCESS);
    }
}
