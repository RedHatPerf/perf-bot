package io.perf.tools.bot.service.job;

import java.io.IOException;
import java.util.Map;

public interface JobRunner {
    /**
     *
     * @param configId configuration id
     * @param jobId job identifier
     * @param params additional job parameters
     * @return the triggered job location
     * @throws IOException if something goes wrong
     */
    String buildJob(String configId, String jobId, Map<String, String> params) throws IOException;
}
