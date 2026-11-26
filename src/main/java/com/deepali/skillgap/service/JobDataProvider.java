package com.deepali.skillgap.service;

import com.deepali.skillgap.model.JobPosting;

import java.util.List;

/**
 * Abstraction over "where job postings come from". Implementations:
 *   - StaticJobDataProvider: the original bundled CSV dataset (always works, no network)
 *   - LiveJobDataProvider:   best-effort fetch from a public job API via Jsoup
 *
 * JobMarketDataService (unchanged public API, for backward compatibility)
 * picks between them based on app.jobdata.source, and always falls back
 * to the static provider if the live one fails or is disabled.
 */
public interface JobDataProvider {
    List<JobPosting> fetchPostings() throws JobDataFetchException;

    String getProviderName();
}
