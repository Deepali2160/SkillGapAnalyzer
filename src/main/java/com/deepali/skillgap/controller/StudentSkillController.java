package com.deepali.skillgap.controller;

import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.repository.SkillRepository;
import com.deepali.skillgap.service.CurrentStudentService;
import com.deepali.skillgap.service.StudentSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentSkillController {

    @Autowired
    private StudentSkillService studentSkillService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CurrentStudentService currentStudentService;

    @GetMapping("/skills")
    public String skillsPage(Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        model.addAttribute("student", student);
        model.addAttribute("allSkills", skillRepository.findAll());
        return "skills-manage";
    }

    @PostMapping("/skills/add")
    public String addSkill(@RequestParam String skillName, Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        studentSkillService.addSkill(student, skillName);
        return "redirect:/skills";
    }

    @PostMapping("/skills/remove")
    public String removeSkill(@RequestParam String skillName, Model model) {
        Student student = currentStudentService.requireCurrentStudent();
        studentSkillService.removeSkill(student, skillName);
        return "redirect:/skills";
    }
}
