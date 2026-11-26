package com.deepali.skillgap.dto;

import com.deepali.skillgap.entity.Student;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Safe, outward-facing view of a Student. Deliberately excludes
 * passwordHash - this is what controllers/templates should use instead
 * of the raw Student entity whenever displaying profile data.
 */
public class StudentProfileDto {

    private Long id;
    private String name;
    private String email;
    private List<String> skills;

    public StudentProfileDto() {
    }

    public static StudentProfileDto fromEntity(Student student) {
        StudentProfileDto dto = new StudentProfileDto();
        dto.id = student.getId();
        dto.name = student.getName();
        dto.email = student.getEmail();
        dto.skills = student.getSkills().stream()
                .map(s -> s.getName())
                .sorted()
                .collect(Collectors.toList());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getSkills() {
        return skills;
    }
}
