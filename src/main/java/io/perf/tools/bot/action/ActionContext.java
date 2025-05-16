package io.perf.tools.bot.action;

import io.perf.tools.bot.model.ProjectConfig;
import io.quarkiverse.githubapp.runtime.github.PayloadHelper;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

/**
 * This represents the context of the request/command
 */
public class ActionContext {

    private final GHEventPayload.IssueComment payload;
    private final GHRepository repository;
    private final GHIssue issue;
    private final ProjectConfig projectConfig;
    private ActionResult currentResult;

    public ActionContext(GHEventPayload.IssueComment payload, ProjectConfig projectConfig) {
        this.payload = payload;
        this.repository = PayloadHelper.getRepository(payload);
        this.issue = payload.getIssue();
        this.projectConfig = projectConfig;
        this.currentResult = null;
    }

    public GHEventPayload.IssueComment getPayload() {
        return payload;
    }

    public GHRepository getRepository() {
        return repository;
    }

    public GHIssue getIssue() {
        return issue;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setCurrentResult(ActionResult currentResult) {
        this.currentResult = currentResult;
    }

    public ActionResult getCurrentResult() {
        return currentResult;
    }

    public ActionContext process(Action action) throws IOException {
        return action.perform(this);
    }
}
