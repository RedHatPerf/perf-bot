package io.perf.tools.bot.service;

import io.perf.tools.bot.model.config.ProjectConfig;
import io.perf.tools.bot.service.datastore.Datastore;
import io.perf.tools.bot.service.job.JobExecutor;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that manages project configurations for the performance bot.
 * <p>
 * Stores and provides access to {@link ProjectConfig} instances by repository name or test ID.
 * </p>
 */
@ApplicationScoped
@Startup
public class ConfigService {
    @ConfigProperty(name = "proxy.job.runner.jenkins.check-connection.enabled", defaultValue = "true")
    Boolean checkJobExecutorConnection;

    @ConfigProperty(name = "proxy.datastore.horreum.check-connection.enabled", defaultValue = "true")
    Boolean checkDatastoreConnection;

    @Inject
    Datastore datastore;

    @Inject
    JobExecutor jobExecutor;

    final Map<String, ProjectConfig> configs = new HashMap<>();

    public void loadConfig(ProjectConfig config) {
        boolean validated = true;
        if (checkJobExecutorConnection) {
            validated = jobExecutor.checkConnection(config);
        }
        if (checkDatastoreConnection) {
            validated = datastore.checkConnection(config);
        }

        if (validated) {
            configs.put(config.repoFullName, config);
        } else {
            Log.error("Failed to load config: " + config.repoFullName);
            throw new RuntimeException("Failed to load config: " + config.repoFullName);
        }
    }

    public Map<String, ProjectConfig> getConfigs() {
        return configs;
    }

    /**
     * Gets the project configuration for the specified repository full name.
     *
     * @param repoFullName repository full name (e.g., "owner/repo")
     * @return the matching project configuration or null if not found
     */
    public ProjectConfig getConfig(String repoFullName) {
        return configs.get(repoFullName);
    }

    /**
     * Finds a project configuration by its Horreum test ID.
     *
     * @param testId the Horreum test identifier
     * @return the matching project configuration or null if not found
     */
    public ProjectConfig getConfigByTestId(String testId) {
        return configs.values().stream()
                .filter(pc -> pc.jobs.values().stream().anyMatch(j -> j.datastoreConfig.testId.equals(testId))).findFirst()
                .orElse(null);
    }
}
