package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.BaseAction;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Validate extends BaseAction {
    public static final String NAME = "validate";

    @Override
    public boolean internal() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        // TODO: implement robust validation logic
        // e.g., checking the repository is configured/integrated
        // the user has valid permissions/authorization
    }
}
