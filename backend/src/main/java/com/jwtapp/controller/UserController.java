package com.jwtapp.controller;

import com.jwtapp.dto.ChangePasswordRequest;
import com.jwtapp.dto.UpdateUserRequest;
import com.jwtapp.dto.UserDTO;
import com.jwtapp.service.AuthorizationService;
import com.jwtapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }
        throw new IllegalArgumentException("User ID not found in security context");
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        UUID userId = getCurrentUserId();
        UserDTO userDTO = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        UUID userId = getCurrentUserId();
        UserDTO updatedUser = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        UUID userId = getCurrentUserId();
        authorizationService.checkAdminAccess(userId);
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUserByAdmin(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UUID currentUserId = getCurrentUserId();
        authorizationService.checkAdminAccess(currentUserId);
        UserDTO updatedUser = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUserByAdmin(@PathVariable UUID userId) {
        UUID currentUserId = getCurrentUserId();
        authorizationService.checkAdminAccess(currentUserId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = getCurrentUserId();
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
