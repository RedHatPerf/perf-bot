package io.perf.tools.bot.service.datastore.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.SortDirection;
import io.hyperfoil.tools.horreum.api.data.ExportedLabelValues;
import io.hyperfoil.tools.horreum.api.data.LabelValueMap;
import io.hyperfoil.tools.horreum.api.data.View;
import io.hyperfoil.tools.horreum.api.services.DatasetService;
import io.hyperfoil.tools.horreum.api.services.ExperimentService;
import io.hyperfoil.tools.horreum.api.services.RunService;
import io.perf.tools.bot.model.config.JobDef;
import io.perf.tools.bot.model.config.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.datastore.Datastore;
import io.perf.tools.bot.service.datastore.horreum.util.DatasetSummaryConverter;
import io.perf.tools.bot.service.datastore.horreum.util.ExperimentResultConverter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HorreumService implements Datastore {
    public static final String LATEST_RUN = "latest";

    @Inject
    ConfigService configService;

    @Inject
    ExperimentResultConverter experimentResultConverter;

    @Inject
    DatasetSummaryConverter datasetSummaryConverter;

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

    public View getView(HorreumClient client, JobDef jobDef) {
        List<View> views = client.uiService.getViews(Integer.parseInt(jobDef.datastoreConfig.testId));
        // get the first view that matches the viewId (if not null) otherwise it matches the "default" name
        Optional<View> found = views.stream().filter(v -> jobDef.datastoreConfig.viewId == null
                ? "default".equalsIgnoreCase(v.name)
                : v.id == Integer.parseInt(jobDef.datastoreConfig.viewId)).findFirst();

        if (found.isEmpty()) {
            throw new RuntimeException(
                    "Cannot find view for test " + jobDef.datastoreConfig.testId + " with id " + jobDef.datastoreConfig.viewId);
        }
        return found.get();
    }

    /**
     * Retrieves a summarized view of a specific Horreum run.
     * Finally, it serializes a summary of the first dataset found along with the view.
     *
     * @param repoFullName The full name of the repository (e.g., 'owner/repo').
     * @param jobId        The ID of the job.
     * @param horreumRunId The ID of the Horreum run.
     * @return A JSON string representing the summarized view of the run's dataset.
     * @throws RuntimeException If no datasets are found for the specified Horreum run ID.
     */
    public String getRunView(String repoFullName, String jobId, String horreumRunId) {
        Log.trace("Fetching view for run " + horreumRunId + " and job " + jobId);

        ProjectConfig config = configService.getConfig(repoFullName);
        JobDef job = config.jobs.get(jobId);

        try (HorreumClient client = createHorreumClient(config)) {
            View view = getView(client, job);
            Log.trace("Using view " + view.name);
            DatasetService.DatasetList datasets = client.datasetService.listByRun(Integer.parseInt(horreumRunId), null, 1,
                    null, null, null, view.id);

            if (datasets.datasets.isEmpty()) {
                throw new RuntimeException("No datasets found for run " + horreumRunId);
            }
            // assuming there is only one dataset per run!
            return datasetSummaryConverter.serialize(new ImmutablePair<>(datasets.datasets.getFirst(), view));
        }
    }

    /**
     * Retrieves a summarized view of a specific or the latest Horreum run for a given job.
     * If {@code horreumRunId} is equal to {@link #LATEST_RUN}, it fetches the latest run.
     * Otherwise, it retrieves the view for the specified run ID using {@link #getRunView(String, String, String)}.
     *
     * @param repoFullName The full name of the repository (e.g., 'owner/repo').
     * @param jobId        The ID of the job.
     * @param horreumRunId The ID of the Horreum run, or {@link #LATEST_RUN} to get the latest run.
     * @return A JSON string representing the summarized view of the run's dataset.
     * @throws RuntimeException If no runs are found for the test when requesting the latest run.
     */
    @Override
    public String getRun(String repoFullName, String jobId, String horreumRunId) {
        Log.trace("Fetching run " + horreumRunId  + " and job " + jobId);

        ProjectConfig config = configService.getConfig(repoFullName);
        String testId = config.jobs.get(jobId).datastoreConfig.testId;
        try (HorreumClient client = createHorreumClient(config)) {
            if (LATEST_RUN.equals(horreumRunId)) {
                // we need only one run, the latest one
                // TODO: I should filter runs by pull request number!!
                RunService.RunsSummary summary = client.runService.listTestRuns(Integer.parseInt(testId),
                        false, 1, 0, "id", SortDirection.Descending);
                if (summary.runs.isEmpty()) {
                    throw new RuntimeException("No runs found for test " + testId);
                }

                horreumRunId = Integer.toString(summary.runs.getFirst().id);
            }
            return getRunView(repoFullName, jobId, horreumRunId);
        }
    }

    /**
     * Compare the provided run against the baseline configured in Horreum
     *
     * @param jobId job identifier
     * @param horreumRunId id of the run in Horreum
     */
    @Override
    public String compare(String repoFullName, String jobId, String horreumRunId) {
        Log.trace("Comparing run " + horreumRunId + " and job " + jobId);

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
