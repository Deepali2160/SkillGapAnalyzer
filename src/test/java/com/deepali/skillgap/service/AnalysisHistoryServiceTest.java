package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Analysis;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.UnauthorizedAccessException;
import com.deepali.skillgap.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Verifies the core multi-user isolation rule from the spec:
 * "Student A must never be able to request Student B's ... analysis history."
 */
@ExtendWith(MockitoExtension.class)
class AnalysisHistoryServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalysisHistoryService analysisHistoryService;

    @Test
    void studentCannotAccessAnotherStudentsAnalysis() throws Exception {
        Student studentA = new Student("Alice", "alice@example.com", "hashA");
        setId(studentA, 1L);

        Student studentB = new Student("Bob", "bob@example.com", "hashB");
        setId(studentB, 2L);

        Analysis analysisOwnedByB = new Analysis(studentB, "Java Developer", 80.0);
        setId(analysisOwnedByB, 100L);

        when(analysisRepository.findById(100L)).thenReturn(Optional.of(analysisOwnedByB));

        // Student A tries to view Student B's analysis by id -> must be blocked
        assertThrows(UnauthorizedAccessException.class,
                () -> analysisHistoryService.getOwnedAnalysis(100L, studentA));
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
