package io.perf.tools.bot.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the configuration for a specific project.
 * This class holds all necessary details to manage and interact
 * with the project and the defined jobs from GitHub.
 */
public class ProjectConfig {

    /**
     * The unique identifier for this configuration.
     * This could be the repository's full name or any other
     * string that uniquely identifies this project's settings.
     */
    public String id;

    /**
     * The full name of the GitHub repository, in the
     * format "owner/repository_name" (e.g., "quarkusio/quarkus").
     */
    public String repoFullName;

    /**
     * The URL of the GitHub repository.
     * (e.g., "<a href="https://github.com/quarkusio/quarkus">https://github.com/quarkusio/quarkus</a>").
     */
    public String repositoryUrl = "";

    /**
     * A textual description of the project or its configuration.
     */
    public String description = "";

    /**
     * The API key used for authentication when interacting with the Horreum API.
     * This key grants the perf-bot permission to upload runs, create tests,
     * or perform other actions against the Horreum instance.
     * It should be treated as a secret.
     */
    public String datastoreApiKey;

    /**
     * The user associated with API key used for authentication when interacting with the underlying Jobs
     * platform. e.g., Jenkins.
     */
    public String jobPlatformUser;

    /**
     * The API key used for authentication when interacting with the underlying Jobs
     * platform. e.g., Jenkins. This key should grant the perf-bot permission to trigger
     * jobs and monitor their status,
     * It should be treated as a secret.
     */
    public String jobPlatformApiKey;

    /**
     * A list of GitHub usernames who are authorized to interact
     * with the bot.
     */
    public List<String> authorizedUsers = new ArrayList<>();

    /**
     * A map of all jobs that are exposed or configured for this project.
     * The key is a String representing the name or identifier of the job,
     * and the value is a {@link JobDef} object containing the definition.
     */
    public Map<String, JobDef> jobs;
}
