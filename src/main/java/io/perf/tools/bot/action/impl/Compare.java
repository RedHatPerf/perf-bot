package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import io.perf.tools.bot.service.datastore.ResultStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static io.perf.tools.bot.service.datastore.horreum.HorreumService.LATEST_RUN;

@ApplicationScoped
public class Compare extends BaseAction {
    public static final String NAME = "compare";

    @Inject
    ResultStore resultStore;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        //0: prompt; 1: cmd; 2: run id;
        String[] comment = context.getPayload().getComment().getBody().split(" ");
        String runId = comment.length > 2 ? comment[2] : LATEST_RUN;
        try {
            String msg = "## Comparison for " + runId + " against baseline" + "\n\n" + resultStore.compare(
                    context.getProjectConfig().id, runId);
            context.setCurrentResult(ActionResult.success(msg, getName()));
        } catch (Exception e) {
            Log.error("Failed to compare run", e);
            context.setCurrentResult(ActionResult.failure("Failed to compare run: " + e.getMessage(), getName()));
        }

    }
}
