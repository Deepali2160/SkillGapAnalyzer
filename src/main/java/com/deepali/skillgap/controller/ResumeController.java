package com.deepali.skillgap.controller;

import com.deepali.skillgap.entity.Resume;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.repository.ResumeRepository;
import com.deepali.skillgap.service.CurrentStudentService;
import com.deepali.skillgap.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private CurrentStudentService currentStudentService;

    @GetMapping("/resume")
    public String resumePage(Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        resumeRepository.findByStudent(student).ifPresent(r -> model.addAttribute("existingResume", r));
        return "resume-upload";
    }

    @PostMapping("/resume/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file, Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        Resume resume = resumeService.uploadAndParse(student, file);
        model.addAttribute("existingResume", resume);
        model.addAttribute("successMessage", "Resume uploaded and parsed successfully. "
                + resume.getDetectedSkills().size() + " skill(s) detected.");
        return "resume-upload";
    }
}
