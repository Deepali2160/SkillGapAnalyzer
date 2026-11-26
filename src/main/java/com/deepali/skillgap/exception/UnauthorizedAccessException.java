package com.deepali.skillgap.exception;

/**
 * Thrown whenever a student tries to access another student's data
 * (e.g. Student A requesting Student B's analysis history).
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
