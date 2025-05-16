package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

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
    public void proceed(ActionContext context) throws IOException {
        String login = context.getIssue().getUser().getLogin();
        if (context.getProjectConfig().authorizedUsers.contains(login)) {
            context.setCurrentResult(ActionResult.success("User " + login + " authorized", getName()));
        } else {
            context.setCurrentResult(ActionResult.failure(
                    "I am sorry " + login + ", you are not authorized to interact with " + prompt + " in this repository",
                    getName()));
        }
    }
}
