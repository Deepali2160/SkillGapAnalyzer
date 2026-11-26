package com.deepali.skillgap.service;

import com.deepali.skillgap.model.GapResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GapAnalysisServiceTest {

    @Mock
    private DemandAnalysisService demandAnalysisService;

    @InjectMocks
    private GapAnalysisService gapAnalysisService;

    @BeforeEach
    void setUp() {
        Map<String, Double> demand = new LinkedHashMap<>();
        demand.put("Java", 95.0);
        demand.put("Spring Boot", 80.0);
        demand.put("SQL", 70.0);
        demand.put("Docker", 20.0); // below the 25% missing-threshold, should never be flagged as "missing"
        when(demandAnalysisService.getDemandForRole("Java Developer")).thenReturn(demand);
    }

    @Test
    void identifiesMissingHighDemandSkillsOnly() {
        Set<String> studentSkills = Set.of("Java"); // has Java, missing Spring Boot & SQL

        GapResult result = gapAnalysisService.computeGap("Java Developer", studentSkills);

        assertTrue(result.getMissingSkills().contains("Spring Boot"));
        assertTrue(result.getMissingSkills().contains("SQL"));
        assertFalse(result.getMissingSkills().contains("Java")); // student already has it
        assertFalse(result.getMissingSkills().contains("Docker")); // below threshold
    }

    @Test
    void missingSkillsAreRankedByDemandDescending() {
        Set<String> studentSkills = Set.of(); // has nothing

        GapResult result = gapAnalysisService.computeGap("Java Developer", studentSkills);

        assertEquals("Java", result.getMissingSkills().get(0));
        assertEquals("Spring Boot", result.getMissingSkills().get(1));
        assertEquals("SQL", result.getMissingSkills().get(2));
    }

    @Test
    void perfectMatchGivesHundredPercent() {
        Set<String> studentSkills = Set.of("Java", "Spring Boot", "SQL", "Docker");

        GapResult result = gapAnalysisService.computeGap("Java Developer", studentSkills);

        assertEquals(100.0, result.getMatchPercent(), 0.01);
        assertTrue(result.getMissingSkills().isEmpty());
    }

    @Test
    void noOverlapGivesZeroPercent() {
        Set<String> studentSkills = Set.of("Excel", "Tableau"); // no overlap with demand skills

        GapResult result = gapAnalysisService.computeGap("Java Developer", studentSkills);

        assertEquals(0.0, result.getMatchPercent(), 0.01);
    }
}
