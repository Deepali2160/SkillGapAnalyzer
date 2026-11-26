package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.repository.SkillRepository;
import com.deepali.skillgap.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Lets a student add/remove skills on their saved profile. Always reuses
 * the existing Skill taxonomy (SkillRepository) rather than creating new
 * skill rows on the fly.
 */
@Service
public class StudentSkillService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SkillRepository skillRepository;

    public Student addSkill(Student student, String skillName) {
        Optional<Skill> skill = skillRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst();
        skill.ifPresent(value -> student.getSkills().add(value));
        return studentRepository.save(student);
    }

    public Student removeSkill(Student student, String skillName) {
        student.getSkills().removeIf(s -> s.getName().equalsIgnoreCase(skillName));
        return studentRepository.save(student);
    }
}
