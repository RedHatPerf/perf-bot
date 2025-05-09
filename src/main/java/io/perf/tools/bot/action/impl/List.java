package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;

@ApplicationScoped
public class List extends BaseAction {
    public static final String NAME = "list";

    @Inject
    ConfigService configService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        String repoFullName = context.getRepository().getFullName();
        ProjectConfig config = configService.getConfig(repoFullName);
        if (config == null) {
            context.setCurrentResult(ActionResult.failure("Current repository is not configured!", getName()));
        } else {
            String msg = "Available jobs: " + Arrays.toString(config.jobs.keySet().toArray());
            context.setCurrentResult(ActionResult.success(msg, getName()));
        }
    }
}
