package io.perf.tools.bot.action;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Startup
public class ActionResolver {

    final Map<String, Action> internalActions;
    final Map<String, Action> availableActions;

    @Inject
    public ActionResolver(Instance<Action> actions) {
        this.internalActions = actions.stream().filter(Action::internal).collect(Collectors.toMap(Action::getName, Function.identity()));
        this.availableActions = actions.stream().filter(a -> !a.internal()).collect(Collectors.toMap(Action::getName, Function.identity()));

        Log.info("Installed actions: " + Arrays.toString(availableActions.keySet().toArray()));
        Log.info("Internal actions: " + Arrays.toString(internalActions.keySet().toArray()));
    }

    public Action getAction(String name) {
        return availableActions.get(name);
    }

    public Action getActionFromComment(String comment) {
        String[] split = comment.split(" ");
        String cmd = "";
        if (split.length > 1) {
            cmd = split[1];
        }

        return getAction(cmd);
    }
}
