package com.deepali.skillgap.service;

import com.deepali.skillgap.model.GapResult;
import com.deepali.skillgap.model.SkillGapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core "brain" of the project: compares a student's skill-set against the
 * market-demand vector for their target role, and produces:
 *   1. A ranked skill-by-skill comparison (have / missing + demand %)
 *   2. A prioritized list of missing skills (highest-demand first)
 *   3. An overall match score using cosine similarity
 */
@Service
public class GapAnalysisService {

    @Autowired
    private DemandAnalysisService demandAnalysisService;

    // Only skills with demand >= this threshold are flagged as a "gap worth fixing"
    private static final double MISSING_THRESHOLD = 25.0;

    public GapResult computeGap(String role, Set<String> studentSkills) {
        Map<String, Double> demand = demandAnalysisService.getDemandForRole(role);

        List<SkillGapEntry> entries = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        double dot = 0.0;
        double normDemand = 0.0;
        double normStudent = 0.0;

        for (Map.Entry<String, Double> e : demand.entrySet()) {
            String skill = e.getKey();
            double demandPercent = e.getValue();
            boolean has = studentSkills.stream().anyMatch(s -> s.equalsIgnoreCase(skill));

            entries.add(new SkillGapEntry(skill, demandPercent, has));

            double demandVal = demandPercent / 100.0;
            double studentVal = has ? 1.0 : 0.0;

            dot += demandVal * studentVal;
            normDemand += demandVal * demandVal;
            normStudent += studentVal * studentVal;

            if (!has && demandPercent >= MISSING_THRESHOLD) {
                missing.add(skill);
            }
        }

        entries.sort((a, b) -> Double.compare(b.getDemandPercent(), a.getDemandPercent()));
        missing.sort((a, b) -> {
            double da = demand.getOrDefault(a, 0.0);
            double db = demand.getOrDefault(b, 0.0);
            return Double.compare(db, da);
        });

        double matchPercent = 0.0;
        if (normDemand > 0 && normStudent > 0) {
            matchPercent = (dot / (Math.sqrt(normDemand) * Math.sqrt(normStudent))) * 100.0;
        }
        matchPercent = Math.round(matchPercent * 10.0) / 10.0;

        return new GapResult(role, matchPercent, entries, missing);
    }
}
