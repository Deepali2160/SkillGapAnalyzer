package com.deepali.skillgap.controller;

import com.deepali.skillgap.entity.Course;
import com.deepali.skillgap.model.GapResult;
import com.deepali.skillgap.repository.CourseRepository;
import com.deepali.skillgap.repository.SkillRepository;
import com.deepali.skillgap.service.DemandAnalysisService;
import com.deepali.skillgap.service.GapAnalysisService;
import com.deepali.skillgap.service.JobMarketDataService;
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

@Controller
public class HomeController {

    @Autowired
    private DemandAnalysisService demandAnalysisService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private GapAnalysisService gapAnalysisService;

    @Autowired
    private RecommendationService recommendationService;

    // Added only to power the landing-page stats section with real,
    // database-driven numbers instead of hardcoded/invented figures.
    // No existing attribute, endpoint, or behavior was changed.
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JobMarketDataService jobMarketDataService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("roles", demandAnalysisService.getRoles());
        model.addAttribute("allSkills", skillRepository.findAll());

        // Real stats for the landing page (Section 8 of the redesign brief)
        model.addAttribute("jobPostingCount", jobMarketDataService.getAllPostings().size());
        model.addAttribute("skillCount", skillRepository.findAll().size());
        model.addAttribute("courseCount", courseRepository.findAll().size());
        model.addAttribute("roleCount", demandAnalysisService.getRoles().size());

        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam String role,
                           @RequestParam(name = "selectedSkills", required = false) List<String> selectedSkills,
                           Model model) {
        Set<String> studentSkills = selectedSkills == null ? new HashSet<>() : new HashSet<>(selectedSkills);

        GapResult result = gapAnalysisService.computeGap(role, studentSkills);
        Map<String, List<Course>> recommendations = recommendationService.recommend(result.getMissingSkills());

        model.addAttribute("result", result);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("studentSkillCount", studentSkills.size());
        return "result";
    }
}
