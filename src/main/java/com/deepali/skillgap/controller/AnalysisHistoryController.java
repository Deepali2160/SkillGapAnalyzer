package com.deepali.skillgap.controller;

import com.deepali.skillgap.entity.Analysis;
import com.deepali.skillgap.entity.Course;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.service.AnalysisHistoryService;
import com.deepali.skillgap.service.CurrentStudentService;
import com.deepali.skillgap.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lets a student view their past analyses (Phase 9). Every lookup goes
 * through AnalysisHistoryService.getOwnedAnalysis(), which enforces that
 * a student can only ever see their OWN history (Phase 4/12 requirement).
 */
@Controller
public class AnalysisHistoryController {

    @Autowired
    private AnalysisHistoryService analysisHistoryService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CurrentStudentService currentStudentService;

    @GetMapping("/analysis/history")
    public String history(Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        model.addAttribute("history", analysisHistoryService.getHistoryFor(student));
        return "analysis-history";
    }

    @GetMapping("/analysis/history/{id}")
    public String historyDetail(@PathVariable Long id, Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        Analysis analysis = analysisHistoryService.getOwnedAnalysis(id, student);

        List<String> missingSkillNames = analysis.getMissingSkills().stream()
                .map(m -> m.getSkillName())
                .collect(Collectors.toList());
        Map<String, List<Course>> recommendations = recommendationService.recommend(missingSkillNames);

        model.addAttribute("analysis", analysis);
        model.addAttribute("recommendations", recommendations);
        return "analysis-history-detail";
    }
}
