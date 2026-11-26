package com.deepali.skillgap.service;

import com.deepali.skillgap.dto.LoginRequest;
import com.deepali.skillgap.dto.RegisterRequest;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.DuplicateEmailException;
import com.deepali.skillgap.exception.InvalidCredentialsException;
import com.deepali.skillgap.repository.StudentRepository;
import com.deepali.skillgap.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerThrowsOnDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Deepali");
        request.setEmail("deepali@example.com");
        request.setPassword("password123");

        when(studentRepository.existsByEmailIgnoreCase("deepali@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
    }

    @Test
    void registerHashesPasswordBeforeSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Deepali");
        request.setEmail("new@example.com");
        request.setPassword("plainPassword");

        when(studentRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("HASHED_VALUE");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student saved = authService.register(request);

        assertEquals("HASHED_VALUE", saved.getPasswordHash());
        // The raw password must never end up stored anywhere on the entity.
        assertEquals(false, saved.getPasswordHash().equals("plainPassword"));
    }

    @Test
    void loginThrowsForUnknownEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nouser@example.com");
        request.setPassword("whatever");

        when(studentRepository.findByEmailIgnoreCase("nouser@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsForWrongPassword() {
        Student student = new Student("Deepali", "deepali@example.com", "HASHED");
        LoginRequest request = new LoginRequest();
        request.setEmail("deepali@example.com");
        request.setPassword("wrongPassword");

        when(studentRepository.findByEmailIgnoreCase("deepali@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrongPassword", "HASHED")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginReturnsTokenForCorrectCredentials() {
        Student student = new Student("Deepali", "deepali@example.com", "HASHED");
        LoginRequest request = new LoginRequest();
        request.setEmail("deepali@example.com");
        request.setPassword("correctPassword");

        when(studentRepository.findByEmailIgnoreCase("deepali@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("correctPassword", "HASHED")).thenReturn(true);
        when(jwtUtil.generateToken("deepali@example.com")).thenReturn("fake.jwt.token");

        String token = authService.login(request);

        assertEquals("fake.jwt.token", token);
        verify(jwtUtil).generateToken("deepali@example.com");
    }
}
