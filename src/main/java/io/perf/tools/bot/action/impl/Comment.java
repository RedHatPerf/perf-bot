package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

/**
 * This action is responsible to write results back to GitHub
 */
@ApplicationScoped
public class Comment extends BaseAction {
    public static final String NAME = "comment";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean internal() {
        return true;
    }

    /**
     * This is a special comment that writes back to the GitHub issue
     * and this should happen even if the ActionStatus is FAILURE
     * @param context current action context
     * @return updated action context
     */
    @Override
    public ActionContext perform(ActionContext context) {
        proceed(context);
        return context;
    }

    @Override
    public void proceed(ActionContext context) {
        if (context != null && context.getCurrentResult() != null) {
            String message = context.getCurrentResult() != null ? context.getCurrentResult().getMessage() : "";
            if (!message.isBlank()) {
                try {
                    context.getIssue().comment(message);
                } catch (IOException e) {
                    Log.error("Failed to comment issue", e);
                    context.setCurrentResult(ActionResult.failure("Failed to comment issue", getName()));
                }
            }
        }
    }
}
