package io.perf.tools.bot.action.exception;

public abstract class BaseActionException extends Exception {
    public BaseActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
