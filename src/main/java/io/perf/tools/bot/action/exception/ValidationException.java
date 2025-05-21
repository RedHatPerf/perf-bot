package io.perf.tools.bot.action.exception;

public class ValidationException extends ActionExecutionException {
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
