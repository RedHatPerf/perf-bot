package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import io.perf.tools.bot.service.job.JobRunner;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;

@ApplicationScoped
public class Run extends BaseAction {
    public static final String NAME = "run";

    @Inject
    JobRunner jobRunner;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        //0: prompt; 1: cmd; 2: test name; 3...: optional args
        String[] comment = context.getPayload().getComment().getBody().split(" ");
        String testName = comment[2];
        try {
            // TODO: manage parameters
            String location = jobRunner.buildJob(context.getRepository().getFullName(), testName, new HashMap<>());
            context.setCurrentResult(ActionResult.success("Job scheduled to run at " + location, getName()));
        } catch (Exception e) {
            Log.error("Failed to build job", e);
            context.setCurrentResult(ActionResult.failure("Failed to build job: " + e.getMessage(), getName()));
        }
    }
}
