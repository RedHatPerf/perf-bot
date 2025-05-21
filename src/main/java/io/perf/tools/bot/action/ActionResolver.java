package io.perf.tools.bot.action;

import io.perf.tools.bot.action.impl.BaseAction;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An eagerly initialized component responsible for resolving bot actions by name.
 * <p>
 * The {@code ActionResolver} collects all {@link BaseAction} beans at startup and organizes them into two categories:
 * <ul>
 *     <li><b>Available actions</b>: actions exposed to users (i.e., not marked as internal)</li>
 *     <li><b>Internal actions</b>: actions used internally by the bot (e.g., validation, commenting)</li>
 * </ul>
 * </p>
 * <p>
 * It provides methods to:
 * <ul>
 *     <li>Retrieve an action by its name via {@link #getAction(String)}</li>
 *     <li>Parse a user comment to resolve the corresponding command via {@link #getActionFromComment(String)}</li>
 * </ul>
 * </p>
 * <p>
 * The resolver logs all installed actions at startup to help with debugging and visibility.
 * </p>
 *
 * @see BaseAction
 */
@ApplicationScoped
@Startup
public class ActionResolver {

    final Map<String, BaseAction> internalActions;
    final Map<String, BaseAction> availableActions;

    @Inject
    public ActionResolver(Instance<BaseAction> actions) {
        this.internalActions = actions.stream().filter(BaseAction::internal)
                .collect(Collectors.toMap(BaseAction::getName, Function.identity()));
        this.availableActions = actions.stream().filter(a -> !a.internal())
                .collect(Collectors.toMap(BaseAction::getName, Function.identity()));

        Log.info("Installed actions: " + Arrays.toString(availableActions.keySet().toArray()));
        Log.info("Internal actions: " + Arrays.toString(internalActions.keySet().toArray()));
    }

    public BaseAction getAction(String name) {
        return availableActions.get(name);
    }

    public BaseAction getActionFromComment(String comment) {
        String[] split = comment.split(" ");
        String cmd = "";
        if (split.length > 1) {
            cmd = split[1];
        }

        return getAction(cmd);
    }
}
