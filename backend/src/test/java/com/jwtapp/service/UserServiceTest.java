package com.jwtapp.service;

import com.jwtapp.dto.RegisterRequest;
import com.jwtapp.dto.UpdateUserRequest;
import com.jwtapp.dto.UserDTO;
import com.jwtapp.entity.Role;
import com.jwtapp.entity.User;
import com.jwtapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = buildUser(testUserId, "testuser", "test@example.com", "Test", "User", "+1234567890", "Test bio");

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    private User buildUser(UUID id, String username, String email, String firstName, String lastName,
                           String phoneNumber, String bio) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("hashedPassword")
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .bio(bio)
                .enabled(true)
                .role(Role.USER)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void testCreateUserSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserWithDuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(registerRequest),
                "Username already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(registerRequest),
                "Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByUsernameFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(testUserId);

        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getId());
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonexistentId = UUID.randomUUID();
        when(userRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(nonexistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserProfileSuccess() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserProfile(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
    }

    @Test
    void testGetUserProfileNotFound() {
        UUID nonexistentId = UUID.randomUUID();
        when(userRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserProfile(nonexistentId),
                "User not found");
    }

    @Test
    void testUpdateUserProfileSuccess() {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("newemail@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("+9876543210")
                .bio("Updated bio")
                .build();

        User updatedUser = buildUser(testUserId, "testuser", "newemail@example.com", "Updated", "Name",
            "+9876543210", "Updated bio");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateUserProfile(testUserId, updateRequest);

        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
    }

    @Test
    void testUpdateUserProfileWithDuplicateEmail() {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, 
                () -> userService.updateUserProfile(testUserId, updateRequest),
                "Email already exists");
    }

    @Test
    void testUpdateUserProfileNotFound() {
        UUID nonexistentId = UUID.randomUUID();
        UpdateUserRequest updateRequest = new UpdateUserRequest();

        when(userRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
                () -> userService.updateUserProfile(nonexistentId, updateRequest),
                "User not found");
    }

    @Test
    void testGetAllUsers() {
        User user2 = buildUser(UUID.randomUUID(), "testuser2", "test2@example.com", "Test", "User2",
            "+1234567890", "Test bio");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.getFirst().getUsername());
        assertEquals("testuser2", result.get(1).getUsername());
    }

    @Test
    void testGetAllUsersEmpty() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testChangePasswordSuccess() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newHashedPass");

        userService.changePassword(testUserId, "oldPass", "newPass123");

        verify(userRepository).save(testUser);
        assertEquals("newHashedPass", testUser.getPassword());
    }

    @Test
    void testChangePasswordWrongCurrentPassword() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> userService.changePassword(testUserId, "wrongPass", "newPass123"));

        assertEquals("Current password is incorrect", error.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePasswordTooShort() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> userService.changePassword(testUserId, "oldPass", "short"));

        assertEquals("New password must be at least 6 characters long", error.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
