package io.perf.tools.bot.service.datastore.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.data.ExportedLabelValues;
import io.hyperfoil.tools.horreum.api.data.LabelValueMap;
import io.hyperfoil.tools.horreum.api.services.ExperimentService;
import io.hyperfoil.tools.horreum.api.services.RunService;
import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.perf.tools.bot.service.datastore.ResultStore;
import io.perf.tools.bot.service.datastore.horreum.util.ExperimentResultConverter;
import io.perf.tools.bot.service.datastore.horreum.util.LabelValueMapConverter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class HorreumService implements ResultStore {

    @ConfigProperty(name = "proxy.datastore.horreum.url")
    String horreumUrl;

    @Inject
    ConfigService configService;

    @Inject
    LabelValueMapConverter labelValueMapConverter;

    @Inject
    ExperimentResultConverter experimentResultConverter;

    @Override
    public String getRun(String repo, String repositoryUrl, int horreumRunId) {
        check(repo, repositoryUrl);

        ProjectConfig config = configService.getConfig(repo);
        try (HorreumClient client = new HorreumClient.Builder().horreumUrl(horreumUrl).horreumApiKey(config.horreumKey)
                .build()) {
            List<ExportedLabelValues> labelValues = client.runService.getRunLabelValues(horreumRunId, null, null, null, 1000, 0,
                    null, null, false);
            // assuming we have one single dataset
            LabelValueMap labelValueMap = labelValues.getFirst().values;
            return labelValueMapConverter.serialize(labelValueMap);
        }
    }

    /**
     * Compare the provided run against the baseline configured in Horreum
     * @param horreumRunId id of the run in Horreum
     */
    @Override
    public String compare(String repo, String repositoryUrl, int horreumRunId) {
        check(repo, repositoryUrl);
        ProjectConfig config = configService.getConfig(repo);
        try (HorreumClient client = new HorreumClient.Builder().horreumUrl(horreumUrl).horreumApiKey(config.horreumKey)
                .build()) {
            RunService.RunExtended run = client.runService.getRun(horreumRunId);
            // assumption that there is only one dataset
            List<ExperimentService.ExperimentResult> comparisonResults = client.experimentService.runExperiments(
                    run.datasets[0]);
            return String.join("\n", comparisonResults.stream()
                    .map(experimentResult -> experimentResultConverter.serialize(experimentResult)).toList());
        }
    }

    private void check(String repo, String repositoryUrl) {
        if (!configService.getConfigs().containsKey(repo) || !configService.getConfig(repo).repository.equals(
                repositoryUrl)) {
            throw new RuntimeException("Trying to get data for test " + repo + " from " + repositoryUrl);
        }
    }
}
