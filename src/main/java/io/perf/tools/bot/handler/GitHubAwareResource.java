package io.perf.tools.bot.handler;

import io.perf.tools.bot.service.ConfigService;
import io.quarkiverse.githubapp.runtime.github.GitHubService;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class GitHubAwareResource {
    @ConfigProperty(name = "perf.bot.installation.id")
    Long installationId;

    @Inject
    GitHubService gitHubService;

    @Inject
    ConfigService configService;
}
