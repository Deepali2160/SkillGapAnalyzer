package com.deepali.skillgap.config;

import com.deepali.skillgap.entity.Course;
import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.repository.CourseRepository;
import com.deepali.skillgap.repository.SkillRepository;
import com.deepali.skillgap.service.DemandAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Runs once at application startup:
 *   1. Seeds the skills taxonomy (from data/skills.txt) into the DB.
 *   2. Seeds the course catalog (from data/courses.csv) into the DB.
 *   3. Triggers DemandAnalysisService to compute the market skill-demand
 *      profile for every role, so it's ready before any user hits the UI.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DemandAnalysisService demandAnalysisService;

    @Override
    public void run(String... args) throws Exception {
        if (skillRepository.count() == 0) {
            seedSkills();
        }
        if (courseRepository.count() == 0) {
            seedCourses();
        }
        demandAnalysisService.computeAll();
        System.out.println("Data seeding and demand analysis complete.");
    }

    private void seedSkills() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/skills.txt");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    skillRepository.save(new Skill(line));
                }
            }
        }
        System.out.println("Skills taxonomy seeded: " + skillRepository.count() + " skills.");
    }

    private void seedCourses() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/courses.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split("\\|");
                if (p.length == 5) {
                    courseRepository.save(new Course(
                            p[0].trim(),
                            p[1].trim(),
                            p[2].trim(),
                            Double.parseDouble(p[3].trim()),
                            p[4].trim()
                    ));
                }
            }
        }
        System.out.println("Course catalog seeded: " + courseRepository.count() + " courses.");
    }
}
