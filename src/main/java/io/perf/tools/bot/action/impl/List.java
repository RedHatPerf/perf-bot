package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;

@ApplicationScoped
public class List extends BaseAction {
    public static final String NAME = "list";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        if (context.getProjectConfig() == null) {
            context.setCurrentResult(ActionResult.failure("Current repository is not configured!", getName()));
        } else {
            String msg = "Available jobs: " + Arrays.toString(context.getProjectConfig().jobs.keySet().toArray());
            context.setCurrentResult(ActionResult.success(msg, getName()));
        }
    }
}
