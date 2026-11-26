package com.deepali.skillgap.controller;

import com.deepali.skillgap.entity.Analysis;
import com.deepali.skillgap.entity.Course;
import com.deepali.skillgap.entity.Resume;
import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.model.GapResult;
import com.deepali.skillgap.repository.ResumeRepository;
import com.deepali.skillgap.service.AnalysisHistoryService;
import com.deepali.skillgap.service.CurrentStudentService;
import com.deepali.skillgap.service.DemandAnalysisService;
import com.deepali.skillgap.service.GapAnalysisService;
import com.deepali.skillgap.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Personalized skill-gap analysis for logged-in students (Phase 7-8).
 * Deliberately reuses the EXISTING GapAnalysisService and
 * RecommendationService rather than re-implementing the matching or
 * recommendation logic - only the source of "student skills" is new
 * (profile skills and/or resume-detected skills, instead of a one-off
 * checkbox selection).
 */
@Controller
public class AnalysisController {

    @Autowired
    private DemandAnalysisService demandAnalysisService;

    @Autowired
    private GapAnalysisService gapAnalysisService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private AnalysisHistoryService analysisHistoryService;

    @Autowired
    private CurrentStudentService currentStudentService;

    @GetMapping("/analysis")
    public String analysisForm(Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        model.addAttribute("roles", demandAnalysisService.getRoles());
        model.addAttribute("profileSkillCount", student.getSkills().size());
        resumeRepository.findByStudent(student).ifPresent(r -> model.addAttribute("resumeSkillCount", r.getDetectedSkills().size()));
        return "analysis-form";
    }

    @PostMapping("/analysis/run")
    public String runAnalysis(@RequestParam String role,
                               @RequestParam(defaultValue = "profile") String skillSource,
                               Model model) {
        Student student = currentStudentService.requireCurrentStudent();

        Set<String> studentSkills = resolveSkillSet(student, skillSource);

        GapResult result = gapAnalysisService.computeGap(role, studentSkills);
        Map<String, List<Course>> recommendations = recommendationService.recommend(result.getMissingSkills());

        Analysis savedAnalysis = analysisHistoryService.save(student, result);

        model.addAttribute("result", result);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("studentSkillCount", studentSkills.size());
        model.addAttribute("savedAnalysisId", savedAnalysis.getId());
        model.addAttribute("personalized", true);
        return "result";
    }

    private Set<String> resolveSkillSet(Student student, String skillSource) {
        Set<String> skills = new HashSet<>();
        if ("resume".equalsIgnoreCase(skillSource)) {
            Resume resume = resumeRepository.findByStudent(student).orElse(null);
            if (resume != null) {
                skills = resume.getDetectedSkills().stream().map(Skill::getName).collect(Collectors.toSet());
            }
        } else {
            skills = student.getSkills().stream().map(Skill::getName).collect(Collectors.toSet());
        }
        return skills;
    }
}
