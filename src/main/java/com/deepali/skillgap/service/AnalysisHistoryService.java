package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Analysis;
import com.deepali.skillgap.entity.AnalysisMissingSkill;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.ResourceNotFoundException;
import com.deepali.skillgap.exception.UnauthorizedAccessException;
import com.deepali.skillgap.model.GapResult;
import com.deepali.skillgap.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Persists and retrieves skill-gap analysis history for a student
 * (Phase 9). Ownership is always checked here, so a student can never
 * read another student's saved analysis (Phase 4 / Phase 12 requirement).
 */
@Service
public class AnalysisHistoryService {

    @Autowired
    private AnalysisRepository analysisRepository;

    public Analysis save(Student student, GapResult result) {
        Analysis analysis = new Analysis(student, result.getRole(), result.getMatchPercent());
        for (String missingSkill : result.getMissingSkills()) {
            double demandPercent = result.getEntries().stream()
                    .filter(e -> e.getSkillName().equalsIgnoreCase(missingSkill))
                    .findFirst()
                    .map(e -> e.getDemandPercent())
                    .orElse(0.0);
            analysis.getMissingSkills().add(new AnalysisMissingSkill(analysis, missingSkill, demandPercent));
        }
        return analysisRepository.save(analysis);
    }

    public List<Analysis> getHistoryFor(Student student) {
        return analysisRepository.findByStudentOrderByAnalyzedAtDesc(student);
    }

    /**
     * Fetches one saved analysis, but ONLY if it belongs to the given
     * student - otherwise throws, rather than silently returning someone
     * else's data.
     */
    public Analysis getOwnedAnalysis(Long analysisId, Student student) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found."));
        if (!analysis.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedAccessException("This analysis does not belong to the current student.");
        }
        return analysis;
    }
}
