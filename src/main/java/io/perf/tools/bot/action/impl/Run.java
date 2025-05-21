package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.exception.ActionExecutionException;
import io.perf.tools.bot.action.exception.EventNotSupportedException;
import io.perf.tools.bot.model.JobDef;
import io.perf.tools.bot.service.job.JobExecutor;
import io.quarkiverse.githubapp.runtime.github.PayloadHelper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An action responsible for running a specified job based on an issue comment event.
 * <p>
 * This action extends {@link BaseAction} and listens for {@link GHEventPayload.IssueComment} events.
 * When triggered, it parses the issue comment to identify the job name and parameters, validates
 * configurable parameters, and delegates the job execution to {@link JobExecutor}.
 * </p>
 * <p>
 * If the job execution is successful, it updates the action context status to SUCCESS;
 * otherwise, it marks the status as FAILED with the relevant error.
 * </p>
 * <p>
 * The action name is {@value #NAME}.
 * </p>
 */
@ApplicationScoped
public class Run extends BaseAction {
    public static final String NAME = "run";
    private static final Pattern pattern = Pattern.compile("(\\w+)=((\"[^\"]*\")|[^,]*)");

    @Inject
    JobExecutor jobExecutor;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void proceed(ActionContext<?> ctx) throws IOException {
        if (ctx.getPayload() instanceof GHEventPayload.IssueComment issueComment) {
            //0: prompt; 1: cmd; 2: test name; 3...: optional args
            String[] comment = issueComment.getComment().getBody().split(" ");
            String testName = comment[2];

            JobDef job = ctx.getProjectConfig().jobs.get(testName);
            Map<String, String> params = new HashMap<>();
            Matcher matcher = pattern.matcher(issueComment.getComment().getBody());

            if (job.pullRequestNumberParam != null && !job.pullRequestNumberParam.isBlank()) {
                params.put(job.pullRequestNumberParam, Integer.toString(issueComment.getIssue().getNumber()));
            }

            if (job.repoCommitParam != null && !job.repoCommitParam.isBlank()) {
                // TODO: find a way to fetch current commit of the PR
                params.put(job.repoCommitParam, "");
            }

            while (matcher.find()) {
                String key = matcher.group(1);
                String rawValue = matcher.group(2);
                String value = rawValue.startsWith("\"") && rawValue.endsWith("\"")
                        ? rawValue.substring(1, rawValue.length() - 1)
                        : rawValue;
                if (job.configurableParams.containsKey(key)) {
                    params.put(key, value);
                } else {
                    Log.warn("Parameter not configurable: " + key);
                }
            }

            try {
                String location = jobExecutor.buildJob(PayloadHelper.getRepository(issueComment).getFullName(), testName, params);
                Log.info("Job scheduled to run at: " + location);
                ctx.setStatus(ActionContext.Status.SUCCESS)
                        .setMessage("Job scheduled to run")
                        .setError(null);
            } catch (Exception e) {
                Log.error("Failed to build job", e);
                ctx.setStatus(ActionContext.Status.FAILED)
                        .setMessage("Failed to execute the job")
                        .setError(new ActionExecutionException("Failed to execute the job", e));
            }

        } else {
            ctx.setStatus(ActionContext.Status.FAILED)
                    .setMessage("Event not supported")
                    .setError(new EventNotSupportedException(
                            "Event " + ctx.getPayload().getClass().getSimpleName() + " not supported for " + this.getClass()
                                    .getSimpleName(), null));
        }
    }
}
