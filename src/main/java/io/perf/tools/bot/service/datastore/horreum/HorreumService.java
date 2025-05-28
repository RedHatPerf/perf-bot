package io.perf.tools.bot.service.datastore.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.SortDirection;
import io.hyperfoil.tools.horreum.api.data.ExportedLabelValues;
import io.hyperfoil.tools.horreum.api.data.LabelValueMap;
import io.hyperfoil.tools.horreum.api.services.ExperimentService;
import io.hyperfoil.tools.horreum.api.services.RunService;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.datastore.Datastore;
import io.perf.tools.bot.service.datastore.horreum.util.ExperimentResultConverter;
import io.perf.tools.bot.service.datastore.horreum.util.LabelValueMapConverter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class HorreumService implements Datastore {
    public static final String LATEST_RUN = "latest";

    @Inject
    ConfigService configService;

    @Inject
    LabelValueMapConverter labelValueMapConverter;

    @Inject
    ExperimentResultConverter experimentResultConverter;

    @Inject
    HorreumClient.Builder horreumClientBuilder;

    private HorreumClient createHorreumClient(ProjectConfig config) {
        return horreumClientBuilder.horreumApiKey(config.datastoreApiKey).build();
    }

    @Override
    public boolean checkConnection(ProjectConfig config) {
        try (HorreumClient client = createHorreumClient(config)) {
            // TODO: this is not enough to validate the api key
            io.hyperfoil.tools.horreum.api.services.ConfigService.VersionInfo info = client.configService.version();
            Log.info("Connection with Horreum " + info.version + " verified!");
            return true;
        } catch (Exception e) {
            Log.error("Unable to verify connection with Horreum datastore", e);
            return false;
        }
    }

    public LabelValueMap getRun(ProjectConfig config, String horreumRunId) {
        try (HorreumClient client = createHorreumClient(config)) {
            List<ExportedLabelValues> labelValues = client.runService.getRunLabelValues(Integer.parseInt(horreumRunId), null,
                    null, null, 1000, 0,
                    null, null, false);
            // assuming we have one single dataset
            // TODO: implement validation on the result to return meaningful errors in case something is not expected
            return labelValues.getFirst().values;
        }
    }

    // FIXME: users should request run for a specific job
    @Override
    public String getRun(String repoFullName, String jobId, String horreumRunId) {

        ProjectConfig config = configService.getConfig(repoFullName);
        try (HorreumClient client = createHorreumClient(config)) {
            List<ExportedLabelValues> labelValues;
            if (LATEST_RUN.equals(horreumRunId)) {
                // we need only one run, the latest one
                // TODO: I should filter runs by pull request id using the filter param
                labelValues = client.testService.getTestLabelValues(
                        Integer.parseInt(config.jobs.get(jobId).datastoreConfig.testId), null, null, null,
                        false, true, "id", "descending", 1, 0, null, null, false);
            } else {
                labelValues = client.runService.getRunLabelValues(Integer.parseInt(horreumRunId), null, null, null, 1000, 0,
                        null, null, false);
            }
            // assuming we have one single dataset
            // TODO: implement validation on the result to return meaningful errors in case something is not expected
            Log.info("Retrieving run " + horreumRunId + " for job " + jobId);
            LabelValueMap labelValueMap = labelValues.getFirst().values;
            return labelValueMapConverter.serialize(labelValueMap);
        }
    }

    /**
     * Compare the provided run against the baseline configured in Horreum
     *
     * @param jobId job identifier
     * @param horreumRunId id of the run in Horreum
     */
    // FIXME: users should request comparison for a specific job
    @Override
    public String compare(String repoFullName, String jobId, String horreumRunId) {
        ProjectConfig config = configService.getConfig(repoFullName);
        try (HorreumClient client = createHorreumClient(config)) {
            RunService.RunSummary run;
            if (LATEST_RUN.equals(horreumRunId)) {
                // we need only one run, the latest one
                // TODO: I should filter runs by pull request id
                RunService.RunsSummary summary = client.runService.listTestRuns(
                        Integer.parseInt(config.jobs.get(jobId).datastoreConfig.testId), false,
                        1, 1, "id",
                        SortDirection.Descending);
                run = summary.runs.getFirst();
            } else {
                run = client.runService.getRunSummary(Integer.parseInt(horreumRunId));
            }
            Log.info("Comparing run " + run.id + " for job " + jobId);
            // assumption that there is only one dataset
            List<ExperimentService.ExperimentResult> comparisonResults = client.experimentService.runExperiments(
                    run.datasets[0]);
            return String.join("\n", comparisonResults.stream()
                    .map(experimentResult -> experimentResultConverter.serialize(experimentResult)).toList());
        }
    }
}
