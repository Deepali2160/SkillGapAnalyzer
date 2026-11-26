package com.deepali.skillgap.repository;

import com.deepali.skillgap.entity.Resume;
import com.deepali.skillgap.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByStudent(Student student);

    void deleteByStudent(Student student);
}
