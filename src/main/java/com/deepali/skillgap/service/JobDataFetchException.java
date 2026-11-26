package com.deepali.skillgap.service;

/**
 * Thrown by a JobDataProvider when it cannot produce job postings
 * (network failure, timeout, malformed/empty response, etc.).
 * JobMarketDataService catches this and falls back to static data.
 */
public class JobDataFetchException extends Exception {
    public JobDataFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobDataFetchException(String message) {
        super(message);
    }
}
