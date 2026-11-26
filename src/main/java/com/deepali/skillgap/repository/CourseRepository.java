package com.deepali.skillgap.repository;

import com.deepali.skillgap.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findBySkillNameIgnoreCase(String skillName);
}
