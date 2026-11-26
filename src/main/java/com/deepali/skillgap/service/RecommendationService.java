package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Course;
import com.deepali.skillgap.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Content-based recommendation engine: for every missing skill, looks up
 * the best-rated matching course(s) from the curated course catalog.
 */
@Service
public class RecommendationService {

    @Autowired
    private CourseRepository courseRepository;

    private static final int MAX_COURSES_PER_SKILL = 2;

    public Map<String, List<Course>> recommend(List<String> missingSkills) {
        Map<String, List<Course>> map = new LinkedHashMap<>();
        for (String skill : missingSkills) {
            List<Course> courses = courseRepository.findBySkillNameIgnoreCase(skill).stream()
                    .sorted(Comparator.comparingDouble(Course::getRating).reversed())
                    .limit(MAX_COURSES_PER_SKILL)
                    .collect(Collectors.toList());
            if (!courses.isEmpty()) {
                map.put(skill, courses);
            }
        }
        return map;
    }
}
