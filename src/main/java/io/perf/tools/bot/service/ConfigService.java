package io.perf.tools.bot.service;

import io.perf.tools.bot.model.ProjectConfig;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

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

    public ProjectConfig getConfig(String repoFullName) {
        return configs.get(repoFullName);
    }

    public ProjectConfig getConfigByTestId(String testId) {
        return configs.values().stream().filter(pc -> pc.horreumTestId.equals(testId)).findFirst().orElse(null);
    }
}
