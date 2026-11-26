package com.deepali.skillgap.service;

import com.deepali.skillgap.dto.LoginRequest;
import com.deepali.skillgap.dto.RegisterRequest;
import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.exception.DuplicateEmailException;
import com.deepali.skillgap.exception.InvalidCredentialsException;
import com.deepali.skillgap.repository.StudentRepository;
import com.deepali.skillgap.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles registration and login. Passwords are hashed with BCrypt before
 * they ever reach the database - the raw password is never persisted or
 * logged.
 */
@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Student register(RegisterRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Student student = new Student(request.getName(), request.getEmail(), hashedPassword);
        return studentRepository.save(student);
    }

    /**
     * Validates credentials and returns a signed JWT on success.
     * Throws InvalidCredentialsException for both "no such user" and
     * "wrong password" - deliberately not distinguishing the two in the
     * error message, to avoid leaking which emails are registered.
     */
    public String login(LoginRequest request) {
        Student student = studentRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), student.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtUtil.generateToken(student.getEmail());
    }
}
