package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Resume;
import com.deepali.skillgap.entity.Skill;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.InvalidResumeException;
import com.deepali.skillgap.repository.ResumeRepository;
import com.deepali.skillgap.repository.SkillRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles resume upload, validation and text extraction (Apache Tika),
 * then reuses the EXISTING SkillExtractionService/taxonomy to detect
 * skills from the extracted text - no duplicate skill-matching logic.
 *
 * Design decision: only the extracted plain text is stored in the DB,
 * not the original binary file. This keeps storage light and avoids
 * persisting sensitive binary documents that aren't needed once the
 * text has been extracted.
 */
@Service
public class ResumeService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".doc", ".docx");

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillExtractionService skillExtractionService;

    private final Tika tika = new Tika();

    public Resume uploadAndParse(Student student, MultipartFile file) {
        validateFile(file);

        String extractedText;
        try {
            extractedText = tika.parseToString(file.getInputStream());
        } catch (IOException | TikaException e) {
            throw new InvalidResumeException("The uploaded file could not be read. It may be corrupted or in an unsupported format.");
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new InvalidResumeException("No readable text was found in the uploaded resume.");
        }

        Set<String> matchedSkillNames = skillExtractionService.extractSkills(extractedText);
        Set<Skill> matchedSkills = new HashSet<>();
        for (String name : matchedSkillNames) {
            skillRepository.findAll().stream()
                    .filter(s -> s.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(matchedSkills::add);
        }

        // One resume per student: replace any existing one.
        resumeRepository.findByStudent(student).ifPresent(resumeRepository::delete);

        Resume resume = new Resume(student, file.getOriginalFilename(), extractedText.trim());
        resume.setDetectedSkills(matchedSkills);
        return resumeRepository.save(resume);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeException("Please choose a resume file to upload.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidResumeException("File is too large. Maximum allowed size is 5 MB.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || ALLOWED_EXTENSIONS.stream().noneMatch(ext -> filename.toLowerCase().endsWith(ext))) {
            throw new InvalidResumeException("Unsupported file type. Please upload a PDF, DOC, or DOCX file.");
        }
    }
}
