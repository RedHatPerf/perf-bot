package io.perf.tools.bot.handler;

import io.perf.tools.bot.model.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * REST resource for managing performance bot project configurations.
 * <p>
 * Exposes endpoints for loading and retrieving project configurations via HTTP.
 * </p>
 * <ul>
 *     <li>{@code POST /config} — Loads a new {@link ProjectConfig} into the system.</li>
 *     <li>{@code GET /config} — Returns all currently loaded configurations.</li>
 * </ul>
 * <p>
 * This resource delegates all configuration logic to the injected {@link ConfigService}.
 * </p>
 *
 * @see ConfigService
 * @see ProjectConfig
 */
@Path("/config")
public class PerfBotConfigResource {

    @Inject
    ConfigService configService;

    /**
     * Loads a new project configuration
     *
     * @param projectConfig the configuration to be loaded
     */
    @POST
    @ResponseStatus(201)
    @Produces(MediaType.APPLICATION_JSON)
    public void loadProjectConfig(ProjectConfig projectConfig) {
        Log.info("Loading project config for " + projectConfig.repository);
        configService.loadConfig(projectConfig);
    }

    /**
     * Returns all currently loaded project configurations.
     *
     * @return list of project configurations
     */
    @GET
    @ResponseStatus(200)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectConfig> getConfigs() {
        return new ArrayList<>(configService.getConfigs().values());
    }
}
