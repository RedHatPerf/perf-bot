package io.perf.tools.bot.service;

import io.perf.tools.bot.model.ProjectConfig;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

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

    final Map<String, ProjectConfig> configs = new HashMap<>();

    public void loadConfig(ProjectConfig config) {
        configs.put(config.id, config);
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
        return configs.values().stream().filter(pc -> pc.horreumTestId.equals(testId)).findFirst().orElse(null);
    }
}
