package com.deepali.skillgap.controller;

import com.deepali.skillgap.dto.LoginRequest;
import com.deepali.skillgap.dto.RegisterRequest;
import com.deepali.skillgap.exception.DuplicateEmailException;
import com.deepali.skillgap.exception.InvalidCredentialsException;
import com.deepali.skillgap.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Registration and login pages. Deliberately implemented as normal
 * Spring MVC form-post endpoints (not JSON REST) to stay consistent
 * with the rest of this Thymeleaf-based application - see README for
 * the design-decision note on JWT-in-cookie vs pure REST.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            authService.register(request);
            model.addAttribute("successMessage", "Account created successfully. Please log in.");
            model.addAttribute("loginRequest", new LoginRequest());
            return "login";
        } catch (DuplicateEmailException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                         BindingResult bindingResult,
                         Model model,
                         HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        try {
            String token = authService.login(request);

            // Store JWT in an HttpOnly cookie so normal browser navigation
            // to protected Thymeleaf pages (e.g. /dashboard) stays authenticated
            // without needing JavaScript to attach an Authorization header.
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours, matches jwt.expiration-ms default
            response.addCookie(jwtCookie);

            return "redirect:/dashboard";
        } catch (InvalidCredentialsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Stateless JWT "logout": simply expire the cookie holding the token.
        // The token itself remains technically valid until it expires, which
        // is a known trade-off of stateless JWT without a server-side
        // blacklist - documented in README under "Remaining limitations".
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return "redirect:/login";
    }
}
