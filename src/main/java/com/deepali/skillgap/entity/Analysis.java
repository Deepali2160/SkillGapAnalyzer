package com.deepali.skillgap.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A saved record of one skill-gap analysis run by a student, so they can
 * view their history later. Course recommendations are NOT persisted here -
 * they are re-derived from the missing-skills list on demand using the
 * existing RecommendationService, to avoid stale/duplicate course data.
 */
@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
    private double matchPercent;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisMissingSkill> missingSkills = new ArrayList<>();

    public Analysis() {
    }

    public Analysis(Student student, String targetRole, double matchPercent) {
        this.student = student;
        this.targetRole = targetRole;
        this.matchPercent = matchPercent;
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public double getMatchPercent() {
        return matchPercent;
    }

    public void setMatchPercent(double matchPercent) {
        this.matchPercent = matchPercent;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public List<AnalysisMissingSkill> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<AnalysisMissingSkill> missingSkills) {
        this.missingSkills = missingSkills;
    }
}
