package com.deepali.skillgap.controller;

import com.deepali.skillgap.dto.StudentProfileDto;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.repository.AnalysisRepository;
import com.deepali.skillgap.repository.ResumeRepository;
import com.deepali.skillgap.service.CurrentStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Main authenticated landing page (Phase 10). Pulls together profile,
 * resume status, and recent analysis history summary - each piece is
 * fetched via the existing repositories/services, nothing duplicated.
 */
@Controller
public class DashboardController {

    @Autowired
    private CurrentStudentService currentStudentService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Student student = currentStudentService.requireCurrentStudent();

        model.addAttribute("profile", StudentProfileDto.fromEntity(student));
        model.addAttribute("hasResume", resumeRepository.findByStudent(student).isPresent());

        var history = analysisRepository.findByStudentOrderByAnalyzedAtDesc(student);
        model.addAttribute("recentAnalyses", history.stream().limit(3).toList());
        model.addAttribute("totalAnalyses", history.size());

        return "dashboard";
    }
}
