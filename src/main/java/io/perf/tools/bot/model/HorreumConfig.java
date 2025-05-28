package io.perf.tools.bot.model;

/**
 * Configuration settings for interacting with a Horreum instance.
 * Horreum is a service used for storing, visualizing, and analyzing
 * performance benchmark results and other time-series data.
 * This class encapsulates the necessary parameters to connect and
 * send data to a specific Horreum setup.
 *
 * @see <a href="https://horreum.hyperfoil.io/">Horreum Documentation</a>
 */
public class HorreumConfig {

    /**
     * The unique ID of the specific Test definition
     * within Horreum to which new run data should be associated.
     * Tests in Horreum define the structure (schema) and metadata for
     * a collection of benchmark results.
     */
    public String testId;

    /**
     * The identifier ID (or name) of a specific Horreum View.
     * Views in Horreum allow for custom querying and presentation of run data.
     * This ID will be used by perf-bot to fetch and display pre-defined
     * visualizations or summaries of the performance data after a run is uploaded.
     */
    public String viewId;
}
