package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Help extends BaseAction {
    public static final String NAME = "help";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        String helpMsg = "**Usage**: `" + prompt + " COMMAND [ARGS]...`\n\n" +
                "**Commands**:\n" +
                " * `help`            Show this help message.\n" +
                " * `list`            List all available jobs for this repository.\n" +
                " * `run JOB`         Trigger a remote job based on the repository configuration.\n" +
                " * `get RUN`         Get the results of a specific execution run.\n" +
                " * `compare RUN`     Compare a run against the configured baseline.\n" +
                "\n" +
                "**Examples**:\n" +
                " * `" + prompt + " run test`\n" +
                " * `" + prompt + " get 1`\n" +
                " * `" + prompt + " compare 5`\n";

        context.setCurrentResult(ActionResult.success(helpMsg, getName()));
    }
}
