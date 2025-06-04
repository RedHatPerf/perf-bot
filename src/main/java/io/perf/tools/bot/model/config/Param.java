package io.perf.tools.bot.model.config;

/**
 * Represents the definition or metadata for a single configurable parameter of a job.
 * This class is typically used within a collection (e.g., a Map in {@link JobDef#configurableParams})
 * to describe parameters that can be configured for a job execution directly from the command.
 */
public class Param {
    /**
     * The name of the configurable parameter.
     * This is the identifier used to refer to this parameter in the job implementation
     */
    public String name;
}
