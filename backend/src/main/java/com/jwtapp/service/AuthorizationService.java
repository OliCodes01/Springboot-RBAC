package com.jwtapp.service;

import com.jwtapp.dto.UserDTO;
import com.jwtapp.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserService userService;

    public void checkAdminAccess(UUID userId) {
        UserDTO user = userService.getUserProfile(userId);
        if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException(Constants.ERROR_ADMIN_REQUIRED);
        }
    }

    public UserDTO getCurrentUser(UUID userId) {
        return userService.getUserProfile(userId);
    }

    public boolean isAdmin(UUID userId) {
        try {
            UserDTO user = userService.getUserProfile(userId);
            return "ADMIN".equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }
}
