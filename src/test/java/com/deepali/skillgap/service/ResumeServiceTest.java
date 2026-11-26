package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.InvalidResumeException;
import com.deepali.skillgap.repository.ResumeRepository;
import com.deepali.skillgap.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests only the validation logic (file type / size / empty file), since
 * that runs before Tika is ever invoked and does not need network or a
 * real PDF/DOCX fixture to verify.
 */
@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillExtractionService skillExtractionService;

    @InjectMocks
    private ResumeService resumeService;

    private final Student student = new Student("Deepali", "deepali@example.com", "hash");

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]);
        assertThrows(InvalidResumeException.class, () -> resumeService.uploadAndParse(student, emptyFile));
    }

    @Test
    void rejectsUnsupportedFileType() {
        MockMultipartFile txtFile = new MockMultipartFile("file", "resume.txt", "text/plain", "hello".getBytes());
        assertThrows(InvalidResumeException.class, () -> resumeService.uploadAndParse(student, txtFile));
    }

    @Test
    void rejectsFileLargerThan5MB() {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile bigFile = new MockMultipartFile("file", "resume.pdf", "application/pdf", bigContent);
        assertThrows(InvalidResumeException.class, () -> resumeService.uploadAndParse(student, bigFile));
    }
}
