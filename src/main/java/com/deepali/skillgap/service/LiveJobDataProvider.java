package com.deepali.skillgap.service;

import com.deepali.skillgap.model.JobPosting;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Best-effort live job-data provider.
 *
 * Fetches a small number of public, unauthenticated job listings via Jsoup
 * (used purely as an HTTP client here, not for scraping HTML off a page
 * that requires login/CAPTCHA) and normalizes them into our JobPosting
 * model. Only postings that can be confidently mapped to one of our four
 * known target roles are kept - anything else is skipped, since the rest
 * of the app (role dropdown, demand analysis) is built around those roles.
 *
 * This is intentionally DISABLED BY DEFAULT (app.jobdata.source=static) so
 * a broken/unreachable external source never affects a demo. It can be
 * turned on via application.properties or an environment variable.
 *
 * IMPORTANT: no authentication bypass, no CAPTCHA solving, no scraping of
 * access-controlled pages. If the source is unreachable, slow, or returns
 * unexpected data, this provider throws JobDataFetchException and
 * JobMarketDataService falls back to the static dataset automatically.
 */
@Component
public class LiveJobDataProvider implements JobDataProvider {

    private static final Logger log = LoggerFactory.getLogger(LiveJobDataProvider.class);

    @Value("${app.jobdata.live-url:https://remoteok.com/api}")
    private String liveUrl;

    @Value("${app.jobdata.timeout-ms:5000}")
    private int timeoutMs;

    private static final Map<String, List<String>> ROLE_KEYWORDS = new LinkedHashMap<>();

    static {
        ROLE_KEYWORDS.put("Java Developer", List.of("java developer", "java engineer"));
        ROLE_KEYWORDS.put("Backend Developer", List.of("backend", "back-end", "back end"));
        ROLE_KEYWORDS.put("Full Stack Developer", List.of("full stack", "fullstack", "full-stack"));
        ROLE_KEYWORDS.put("Data Analyst", List.of("data analyst", "data analytics"));
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<JobPosting> fetchPostings() throws JobDataFetchException {
        String body;
        try {
            Connection.Response response = Jsoup.connect(liveUrl)
                    .userAgent("Mozilla/5.0 (SkillGapAnalyzer student project)")
                    .timeout(timeoutMs)
                    .ignoreContentType(true)
                    .execute();
            body = response.body();
        } catch (Exception e) {
            // Covers connection refused, DNS failure, timeout, HTTP error, etc.
            throw new JobDataFetchException("Could not reach live job-data source: " + liveUrl, e);
        }

        if (body == null || body.isBlank()) {
            throw new JobDataFetchException("Live job-data source returned an empty response");
        }

        List<JobPosting> postings = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // de-duplication key: title + role

        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.isArray()) {
                throw new JobDataFetchException("Unexpected response format from live job-data source");
            }
            for (JsonNode node : root) {
                String position = textOrEmpty(node, "position");
                String description = textOrEmpty(node, "description");
                String tags = node.has("tags") && node.get("tags").isArray() ? node.get("tags").toString() : "";

                if (position.isBlank()) {
                    continue; // malformed entry, skip
                }

                String matchedRole = inferRole(position + " " + tags);
                if (matchedRole == null) {
                    continue; // doesn't map to one of our known roles, skip
                }

                String dedupeKey = matchedRole + "::" + position.toLowerCase();
                if (!seen.add(dedupeKey)) {
                    continue; // duplicate
                }

                String desc = description.isBlank() ? position : description;
                postings.add(new JobPosting(matchedRole, position, desc));
            }
        } catch (JobDataFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new JobDataFetchException("Failed to parse live job-data response", e);
        }

        if (postings.isEmpty()) {
            throw new JobDataFetchException("Live job-data source returned no postings matching known roles");
        }

        log.info("Live job-data provider fetched {} usable postings from {}", postings.size(), liveUrl);
        return postings;
    }

    private String inferRole(String haystack) {
        String lower = haystack.toLowerCase();
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String textOrEmpty(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText("") : "";
    }

    @Override
    public String getProviderName() {
        return "live";
    }
}
