package com.deepali.skillgap.service;

import com.deepali.skillgap.entity.Student;
import com.deepali.skillgap.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Small helper so every controller doesn't have to repeat the same
 * "pull the logged-in student out of the SecurityContext" boilerplate.
 */
@Service
public class CurrentStudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Optional<Student> getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            return studentRepository.findByEmailIgnoreCase(userDetails.getUsername());
        }
        return Optional.empty();
    }

    public Student requireCurrentStudent() {
        return getCurrentStudent()
                .orElseThrow(() -> new IllegalStateException("No authenticated student in context"));
    }
}
