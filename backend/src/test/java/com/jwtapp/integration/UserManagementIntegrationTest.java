package com.jwtapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtapp.dto.ChangePasswordRequest;
import com.jwtapp.dto.UpdateUserRequest;
import com.jwtapp.entity.Role;
import com.jwtapp.entity.User;
import com.jwtapp.repository.UserRepository;
import com.jwtapp.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;
    private UUID userId;

    private User buildUser(String username, String email, Role role) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .role(role)
                .build();
    }

    private Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        User user = buildUser("testuser", "test@example.com", Role.USER);
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();
        jwtToken = jwtUtil.generateToken(userId);
    }

    @Test
    @Order(1)
    @DisplayName("Should get user profile successfully with JWT cookie")
    void testGetProfileSuccess() throws Exception {
        mockMvc.perform(get("/user/profile")
                        .cookie(createJwtCookie(jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")));
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to get profile without token")
    void testGetProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to get profile with invalid token")
    void testGetProfileInvalidToken() throws Exception {
        Cookie invalidCookie = new Cookie("jwt", "invalid-token");
        mockMvc.perform(get("/user/profile")
                        .cookie(invalidCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("Should update user profile successfully")
    void testUpdateProfileSuccess() throws Exception {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .firstName("Updated")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put("/user/profile")
                        .cookie(createJwtCookie(jwtToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    @Order(5)
    @DisplayName("Should change password successfully")
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .build();

        mockMvc.perform(post("/user/change-password")
                        .cookie(createJwtCookie(jwtToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Password changed successfully")));
    }

    @Test
    @Order(6)
    @DisplayName("Should fail password change with wrong current password")
    void testChangePasswordWrongCurrent() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword123")
                .build();

        mockMvc.perform(post("/user/change-password")
                        .cookie(createJwtCookie(jwtToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("Should fail to access admin endpoints without admin role")
    void testAccessAllUsersWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/user/all")
                        .cookie(createJwtCookie(jwtToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("Should allow admin to access all users")
    void testAccessAllUsersAsAdmin() throws Exception {
        User adminUser = buildUser("admin", "admin@example.com", Role.ADMIN);
        User savedAdmin = userRepository.save(adminUser);
        String adminToken = jwtUtil.generateToken(savedAdmin.getId());

        mockMvc.perform(get("/user/all")
                        .cookie(createJwtCookie(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
