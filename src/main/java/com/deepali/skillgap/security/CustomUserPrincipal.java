package com.deepali.skillgap.security;

import com.deepali.skillgap.entity.Student;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts our Student entity to Spring Security's UserDetails contract,
 * without ever exposing the raw entity (or its password hash) beyond
 * the authentication layer.
 */
public class CustomUserPrincipal implements UserDetails {

    private final Student student;

    public CustomUserPrincipal(Student student) {
        this.student = student;
    }

    public Long getStudentId() {
        return student.getId();
    }

    public Student getStudent() {
        return student;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Override
    public String getPassword() {
        return student.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return student.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
