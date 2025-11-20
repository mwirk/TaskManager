package com.taskmanager.org.exception;

public class InvalidUserIdForTaskException extends RuntimeException {
    public InvalidUserIdForTaskException(String message) {
        super(message);
    }
}
