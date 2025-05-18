package com.example.demo;

import com.example.demo.javaSrc.people.*;
import com.example.demo.javaSrc.security.*;
import com.example.demo.javaSrc.worker.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthControllerTest {

    private AuthenticationManager authManager;
    private JwtUtils jwtUtils;
    private PeopleService peopleService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authManager = mock(AuthenticationManager.class);
        jwtUtils = mock(JwtUtils.class);
        peopleService = mock(PeopleService.class);
        authController = new AuthController(authManager, jwtUtils, peopleService);
    }

    @Test
    void testSuccessfulLogin() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        req.setRole("STUDENT");

        Authentication auth = mock(Authentication.class);
        People user = new People();
        user.setEmail("user@example.com");
        user.setRole(People.Role.STUDENT);

        when(authManager.authenticate(any())).thenReturn(auth);
        when(peopleService.findByEmail("user@example.com")).thenReturn(user);
        when(jwtUtils.generateToken(auth)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("token", "jwt-token");
        assertThat(body).containsEntry("role", "STUDENT");
    }

    @Test
    void testLoginWithBadCredentials() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrongpassword");
        req.setRole("STUDENT");

        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("error", "Invalid email or password");
    }

    @Test
    void testLoginWithRoleMismatch() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        req.setRole("TEACHER");

        Authentication auth = mock(Authentication.class);
        People user = new People();
        user.setEmail("user@example.com");
        user.setRole(People.Role.STUDENT); // інша роль

        when(authManager.authenticate(any())).thenReturn(auth);
        when(peopleService.findByEmail("user@example.com")).thenReturn(user);

        ResponseEntity<?> response = authController.login(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("error", "Role mismatch");
    }

    @Test
    void testLoginUserNotFound() {
        AuthRequest req = new AuthRequest();
        req.setEmail("nonexistent@example.com");
        req.setPassword("password");
        req.setRole("STUDENT");

        Authentication auth = mock(Authentication.class);

        when(authManager.authenticate(any())).thenReturn(auth);
        when(peopleService.findByEmail("nonexistent@example.com")).thenReturn(null);

        try {
            authController.login(req);
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }
    }
}

