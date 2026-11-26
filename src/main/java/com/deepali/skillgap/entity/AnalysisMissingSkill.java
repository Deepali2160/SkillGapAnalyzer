package com.deepali.skillgap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One missing-skill line item belonging to a saved Analysis record.
 * Kept as its own entity (rather than a Set<String>) so the demand
 * percentage at the time of analysis is preserved even if market
 * demand changes later.
 */
@Entity
@Table(name = "analysis_missing_skills")
public class AnalysisMissingSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(nullable = false)
    private String skillName;

    @Column(nullable = false)
    private double demandPercent;

    public AnalysisMissingSkill() {
    }

    public AnalysisMissingSkill(Analysis analysis, String skillName, double demandPercent) {
        this.analysis = analysis;
        this.skillName = skillName;
        this.demandPercent = demandPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public double getDemandPercent() {
        return demandPercent;
    }

    public void setDemandPercent(double demandPercent) {
        this.demandPercent = demandPercent;
    }
}
