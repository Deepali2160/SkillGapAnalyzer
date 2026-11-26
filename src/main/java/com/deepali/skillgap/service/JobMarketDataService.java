package com.deepali.skillgap.service;

import com.deepali.skillgap.model.JobPosting;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates WHERE job-posting data comes from (Phase 10).
 *
 * Public API (getAllPostings / getPostingsByRole / getAllRoles) is
 * UNCHANGED from the original implementation, so DemandAnalysisService
 * and everything downstream of it keeps working exactly as before -
 * see Phase 14 (backward compatibility).
 *
 * Behavior:
 *   - app.jobdata.source=static (default): always uses StaticJobDataProvider.
 *   - app.jobdata.source=live: tries LiveJobDataProvider first; if it
 *     throws JobDataFetchException for ANY reason (network failure,
 *     timeout, empty/malformed response), automatically falls back to
 *     StaticJobDataProvider so the application never breaks because an
 *     external source is unavailable.
 */
@Service
public class JobMarketDataService {

    private static final Logger log = LoggerFactory.getLogger(JobMarketDataService.class);

    @Autowired
    private StaticJobDataProvider staticJobDataProvider;

    @Autowired
    private LiveJobDataProvider liveJobDataProvider;

    @Value("${app.jobdata.source:static}")
    private String configuredSource;

    private List<JobPosting> postings = new ArrayList<>();
    private String activeProvider = "static";

    @PostConstruct
    public void loadData() {
        if ("live".equalsIgnoreCase(configuredSource)) {
            try {
                postings = liveJobDataProvider.fetchPostings();
                activeProvider = liveJobDataProvider.getProviderName();
                log.info("Loaded {} job postings from LIVE provider.", postings.size());
                return;
            } catch (JobDataFetchException e) {
                log.warn("Live job-data provider failed ({}). Falling back to static dataset.", e.getMessage());
            }
        }

        try {
            postings = staticJobDataProvider.fetchPostings();
            activeProvider = staticJobDataProvider.getProviderName();
            log.info("Loaded {} job postings from STATIC dataset.", postings.size());
        } catch (JobDataFetchException e) {
            // Should not realistically happen (bundled CSV), but fail safe
            // with an empty list rather than crashing the whole application.
            log.error("Static job-data provider failed unexpectedly: {}", e.getMessage(), e);
            postings = new ArrayList<>();
        }
    }

    public List<JobPosting> getAllPostings() {
        return postings;
    }

    public List<JobPosting> getPostingsByRole(String role) {
        return postings.stream()
                .filter(p -> p.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    public List<String> getAllRoles() {
        return postings.stream()
                .map(JobPosting::getRole)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public String getActiveProviderName() {
        return activeProvider;
    }
}
