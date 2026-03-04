package com.jwtapp.controller;

import com.jwtapp.dto.ChangePasswordRequest;
import com.jwtapp.dto.UpdateUserRequest;
import com.jwtapp.dto.UserDTO;
import com.jwtapp.service.AuthorizationService;
import com.jwtapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserController userController;

    private UUID testUserId;
    private UserDTO testUserDTO;
    private UserDTO adminUserDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserDTO = buildUserDto(testUserId, "testuser", "test@example.com", "Test", "User", "USER");
        adminUserDTO = buildUserDto(UUID.randomUUID(), "admin", "admin@example.com", "Admin", "User", "ADMIN");
        
        SecurityContextHolder.clearContext();
    }

    private UserDTO buildUserDto(UUID id, String username, String email, String firstName, String lastName, String role) {
        return UserDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+1234567890")
                .bio("Test bio")
                .role(role)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    private void setSecurityContext(UUID userId) {
        UsernamePasswordAuthenticationToken token = 
            new UsernamePasswordAuthenticationToken(userId, null, null);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void testGetProfileSuccess() {
        setSecurityContext(testUserId);
        when(userService.getUserProfile(testUserId)).thenReturn(testUserDTO);

        ResponseEntity<UserDTO> response = userController.getProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUserId, response.getBody().getId());
        assertEquals("testuser", response.getBody().getUsername());
        
        verify(userService, times(1)).getUserProfile(testUserId);
    }

    @Test
    @DisplayName("Should update profile successfully")
    void testUpdateProfileSuccess() {
        setSecurityContext(testUserId);
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("newemail@example.com")
                .firstName("Updated")
                .build();

        UserDTO updatedDTO = buildUserDto(testUserId, "testuser", "newemail@example.com", "Updated", "User", "USER");
        when(userService.updateUserProfile(testUserId, updateRequest)).thenReturn(updatedDTO);

        ResponseEntity<UserDTO> response = userController.updateProfile(updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newemail@example.com", response.getBody().getEmail());
        assertEquals("Updated", response.getBody().getFirstName());
        
        verify(userService, times(1)).updateUserProfile(testUserId, updateRequest);
    }

    @Test
    @DisplayName("Should get all users as admin")
    void testGetAllUsersAsAdmin() {
        UUID adminId = UUID.randomUUID();
        setSecurityContext(adminId);
        doNothing().when(authorizationService).checkAdminAccess(adminId);
        List<UserDTO> users = Arrays.asList(testUserDTO, adminUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        
        verify(authorizationService, times(1)).checkAdminAccess(adminId);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should delete user as admin")
    void testDeleteUserByAdmin() {
        UUID adminId = UUID.randomUUID();
        setSecurityContext(adminId);
        doNothing().when(authorizationService).checkAdminAccess(adminId);
        doNothing().when(userService).deleteUser(testUserId);

        ResponseEntity<Map<String, String>> response = userController.deleteUserByAdmin(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody().get("message"));
        
        verify(authorizationService, times(1)).checkAdminAccess(adminId);
        verify(userService, times(1)).deleteUser(testUserId);
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePasswordSuccess() {
        setSecurityContext(testUserId);
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword")
                .build();
        doNothing().when(userService).changePassword(testUserId, "oldPassword", "newPassword");

        ResponseEntity<Map<String, String>> response = userController.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully", response.getBody().get("message"));
        
        verify(userService, times(1)).changePassword(testUserId, "oldPassword", "newPassword");
    }
}
