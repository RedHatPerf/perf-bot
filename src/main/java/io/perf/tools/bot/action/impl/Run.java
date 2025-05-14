package io.perf.tools.bot.action.impl;

import io.perf.tools.bot.action.ActionContext;
import io.perf.tools.bot.action.ActionResult;
import io.perf.tools.bot.action.BaseAction;
import io.perf.tools.bot.model.JobDef;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.job.JobRunner;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class Run extends BaseAction {
    public static final String NAME = "run";
    private static final Pattern pattern = Pattern.compile("(\\w+)=((\"[^\"]*\")|[^,]*)");

    @Inject
    JobRunner jobRunner;

    @Inject
    ConfigService configService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void proceed(ActionContext context) {
        //0: prompt; 1: cmd; 2: test name; 3...: optional args
        String[] comment = context.getPayload().getComment().getBody().split(" ");
        String testName = comment[2];

        JobDef job = configService.getConfig(context.getRepository().getFullName()).jobs.get(testName);
        Map<String, String> params = new HashMap<>();
        Matcher matcher = pattern.matcher(context.getPayload().getComment().getBody());

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
            String location = jobRunner.buildJob(context.getRepository().getFullName(), testName, params);
            context.setCurrentResult(ActionResult.success("Job scheduled to run at " + location, getName()));
        } catch (Exception e) {
            Log.error("Failed to build job", e);
            context.setCurrentResult(ActionResult.failure("Failed to build job: " + e.getMessage(), getName()));
        }
    }
}
