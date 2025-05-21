package io.perf.tools.bot.service.datastore;

public interface Datastore {
    /**
     * Retrieve a specific run execution result
     * @param repo repository full name
     * @param runId run to compare
     * @return results as string
     */
    String getRun(String repo, String runId);


    /**
     * Compare the specified run against a pre-defined baseline
     * @param repo repository full name
     * @param runId run to compare
     * @return comparison results as string
     */
    String compare(String repo, String runId);
}
