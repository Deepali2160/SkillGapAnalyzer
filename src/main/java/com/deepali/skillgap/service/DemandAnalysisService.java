package com.deepali.skillgap.service;

import com.deepali.skillgap.model.JobPosting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds a "Market Skill-Demand Profile" for every job role, by extracting
 * skills from each job posting and computing what percentage of postings
 * (for that role) mention each skill.
 *
 * Example: if 8 out of 10 "Java Developer" postings mention "Spring Boot",
 * then Spring Boot's demand for that role = 80%.
 */
@Service
public class DemandAnalysisService {

    @Autowired
    private JobMarketDataService jobMarketDataService;

    @Autowired
    private SkillExtractionService skillExtractionService;

    private final Map<String, LinkedHashMap<String, Double>> roleSkillDemand = new HashMap<>();

    /**
     * Called once at application startup (after skills taxonomy has been seeded)
     * by DataSeeder, so the demand map is ready before any user request arrives.
     */
    public void computeAll() {
        roleSkillDemand.clear();
        List<String> roles = jobMarketDataService.getAllRoles();

        for (String role : roles) {
            List<JobPosting> postings = jobMarketDataService.getPostingsByRole(role);
            Map<String, Integer> countMap = new HashMap<>();

            for (JobPosting jp : postings) {
                Set<String> skills = skillExtractionService.extractSkills(jp.getTitle() + " " + jp.getDescription());
                for (String s : skills) {
                    countMap.merge(s, 1, Integer::sum);
                }
            }

            int total = Math.max(postings.size(), 1);
            LinkedHashMap<String, Double> percentMap = countMap.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Math.round((e.getValue() * 100.0 / total) * 10.0) / 10.0,
                            (x, y) -> x,
                            LinkedHashMap::new
                    ));
            roleSkillDemand.put(role, percentMap);
        }
        System.out.println("Demand analysis computed for roles: " + roleSkillDemand.keySet());
    }

    public Map<String, Double> getDemandForRole(String role) {
        return roleSkillDemand.getOrDefault(role, new LinkedHashMap<>());
    }

    public List<String> getRoles() {
        return new ArrayList<>(roleSkillDemand.keySet());
    }
}
