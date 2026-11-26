package com.deepali.skillgap.repository;

import com.deepali.skillgap.entity.Analysis;
import com.deepali.skillgap.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByStudentOrderByAnalyzedAtDesc(Student student);

    Optional<Analysis> findByIdAndStudent(Long id, Student student);
}
