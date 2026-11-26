package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillExtractionServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillExtractionService skillExtractionService;

    @BeforeEach
    void setUp() {
        when(skillRepository.findAll()).thenReturn(List.of(
                new Skill("Java"),
                new Skill("Spring Boot"),
                new Skill("SQL"),
                new Skill("Git"),
                new Skill("REST API")
        ));
    }

    @Test
    void extractsSkillsMentionedInText() {
        String text = "We need a developer skilled in Java, Spring Boot, SQL and Git.";
        Set<String> found = skillExtractionService.extractSkills(text);

        assertTrue(found.contains("Java"));
        assertTrue(found.contains("Spring Boot"));
        assertTrue(found.contains("SQL"));
        assertTrue(found.contains("Git"));
        assertFalse(found.contains("REST API"));
    }

    @Test
    void doesNotMatchPartialWords() {
        // "Javascript" should NOT trigger a match for the skill "Java"
        String text = "Looking for a Javascript expert.";
        Set<String> found = skillExtractionService.extractSkills(text);

        assertFalse(found.contains("Java"));
    }

    @Test
    void returnsEmptySetForBlankText() {
        assertEquals(0, skillExtractionService.extractSkills("").size());
        assertEquals(0, skillExtractionService.extractSkills(null).size());
    }
}
