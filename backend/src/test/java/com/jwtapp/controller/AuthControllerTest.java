package com.jwtapp.controller;

import com.jwtapp.dto.AuthResponse;
import com.jwtapp.dto.LoginRequest;
import com.jwtapp.dto.RegisterRequest;
import com.jwtapp.entity.Role;
import com.jwtapp.entity.User;
import com.jwtapp.exception.InvalidCredentialsException;
import com.jwtapp.exception.UserAlreadyExistsException;
import com.jwtapp.service.UserService;
import com.jwtapp.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private UUID testUserId;
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = buildUser(testUserId, "testuser", "test@example.com");
        registerRequest = buildRegisterRequest("testuser", "test@example.com");
        loginRequest = buildLoginRequest("testuser", "password123");
    }

    private User buildUser(UUID id, String username, String email) {
        return User.builder()
            .id(id)
            .username(username)
            .email(email)
            .password("hashedPassword")
            .firstName("Test")
            .lastName("User")
            .role(Role.USER)
            .enabled(true)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    private RegisterRequest buildRegisterRequest(String username, String email) {
        return RegisterRequest.builder()
            .username(username)
            .email(email)
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();
    }

    private LoginRequest buildLoginRequest(String username, String password) {
        return LoginRequest.builder()
            .username(username)
            .password(password)
            .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        String testToken = "test-jwt-token";

        when(userService.createUser(any(RegisterRequest.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUserId)).thenReturn(testToken);
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().getMessage());
        
        verify(userService, times(1)).createUser(any(RegisterRequest.class));
        verify(jwtUtil, times(1)).generateToken(testUserId);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username is taken")
    void testRegisterFailureDuplicateUsername() {
        when(userService.createUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authController.register(registerRequest);
        });

        verify(userService, times(1)).createUser(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is taken")
    void testRegisterFailureDuplicateEmail() {
        when(userService.createUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authController.register(registerRequest);
        });

        verify(userService, times(1)).createUser(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        String testToken = "test-jwt-token";

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(testUserId)).thenReturn(testToken);
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().getMessage());
        
        verify(userService, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword");
        verify(jwtUtil, times(1)).generateToken(testUserId);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void testLoginFailureUserNotFound() {
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        LoginRequest badLoginRequest = buildLoginRequest("nonexistent", "password123");

        assertThrows(InvalidCredentialsException.class, () -> {
            authController.login(badLoginRequest);
        });

        verify(userService, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void testLoginFailureWrongPassword() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        LoginRequest badLoginRequest = buildLoginRequest("testuser", "wrongpassword");

        assertThrows(InvalidCredentialsException.class, () -> {
            authController.login(badLoginRequest);
        });

        verify(userService, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "hashedPassword");
        verify(jwtUtil, never()).generateToken(any(UUID.class));
    }
}
