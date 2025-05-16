package io.perf.tools.bot.action;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

public abstract class BaseAction implements Action {

    @ConfigProperty(name = "perf.bot.prompt")
    protected String prompt;

    // this is going t obe executed if and only if the ActionContext is not FAILED
    abstract public void proceed(ActionContext context) throws IOException;

    @Override
    public boolean internal() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public ActionContext perform(ActionContext context) throws IOException {
        if (context.getCurrentResult() != null && !ActionStatus.SUCCESS.equals(context.getCurrentResult().getStatus())) {
            // skip current execution and return
            return context;
        }
        proceed(context);
        return context;
    }
}
