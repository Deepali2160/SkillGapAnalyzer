package com.deepali.skillgap.service;

import com.deepali.skillgap.model.JobPosting;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The original, always-available data source: reads the bundled
 * data/job_postings.csv (40 postings across 4 roles). This is the
 * fallback used whenever live fetching is disabled or fails.
 */
@Component
public class StaticJobDataProvider implements JobDataProvider {

    @Override
    public List<JobPosting> fetchPostings() throws JobDataFetchException {
        List<JobPosting> postings = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("data/job_postings.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    postings.add(new JobPosting(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                }
            }
        } catch (IOException e) {
            // The static CSV is bundled inside the jar - this should never
            // realistically fail, but we still surface it properly rather
            // than swallowing it, since there is no further fallback below this.
            throw new JobDataFetchException("Failed to read bundled static job-postings dataset", e);
        }
        return postings;
    }

    @Override
    public String getProviderName() {
        return "static";
    }
}
