package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A lightweight, pure-Java "NLP" skill-extraction engine.
 * Instead of a heavy ML model, it tokenizes/matches job-description text
 * against a curated skills taxonomy (stored in the skills table) using
 * case-insensitive, word-boundary regex matching. This keeps the whole
 * pipeline dependency-free and fully explainable for a viva.
 */
@Service
public class SkillExtractionService {

    @Autowired
    private SkillRepository skillRepository;

    public Set<String> extractSkills(String text) {
        Set<String> found = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return found;
        }
        List<Skill> allSkills = skillRepository.findAll();
        String lowerText = text.toLowerCase();

        for (Skill skill : allSkills) {
            String skillLower = skill.getName().toLowerCase();
            String pattern = "\\b" + Pattern.quote(skillLower) + "\\b";
            if (Pattern.compile(pattern).matcher(lowerText).find()) {
                found.add(skill.getName());
            }
        }
        return found;
    }
}
