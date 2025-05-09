package io.perf.tools.bot.action;

public class ActionResult {
    private final String message;
    private final ActionStatus status;
    private final String step;

    ActionResult(String message, ActionStatus status, String step) {
        this.message = message;
        this.status = status;
        this.step = step;
    }

    public String getMessage() {
        return message;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public static ActionResult success(String message, String step) {
        return new ActionResult(message, ActionStatus.SUCCESS, step);
    }

    public static ActionResult failure(String message, String step) {
        return new ActionResult(message, ActionStatus.FAILURE, step);
    }

    @Override
    public String toString() {
        return "[" + step + "]: " + message;
    }
}
