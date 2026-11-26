package com.deepali.skillgap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores the result of parsing a student's uploaded resume.
 *
 * Design decision: we do NOT store the original binary file in the database.
 * Only the extracted plain text (via Apache Tika) and the detected skills are
 * persisted. This keeps the DB small and avoids storing sensitive binary
 * blobs that aren't needed once text extraction is done. See ResumeService.
 */
@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(nullable = false)
    private String originalFileName;

    @Lob
    @Column(length = 20000)
    private String extractedText;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "resume_skills",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> detectedSkills = new HashSet<>();

    public Resume() {
    }

    public Resume(Student student, String originalFileName, String extractedText) {
        this.student = student;
        this.originalFileName = originalFileName;
        this.extractedText = extractedText;
        this.uploadedAt = LocalDateTime.now();
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Set<Skill> getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(Set<Skill> detectedSkills) {
        this.detectedSkills = detectedSkills;
    }
}
