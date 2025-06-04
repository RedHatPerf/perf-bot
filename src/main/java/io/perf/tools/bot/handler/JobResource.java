package io.perf.tools.bot.handler;

import io.perf.tools.bot.model.config.ProjectConfig;
import io.perf.tools.bot.service.ConfigService;

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
public class JobResource {
}
