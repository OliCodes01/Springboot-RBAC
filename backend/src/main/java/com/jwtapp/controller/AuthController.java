package com.jwtapp.controller;

import com.jwtapp.dto.AuthResponse;
import com.jwtapp.dto.LoginRequest;
import com.jwtapp.dto.RegisterRequest;
import com.jwtapp.entity.User;
import com.jwtapp.exception.InvalidCredentialsException;
import com.jwtapp.service.UserService;
import com.jwtapp.util.Constants;
import com.jwtapp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(request);
        String token = jwtUtil.generateToken(user.getId());
        ResponseCookie cookie = buildAuthCookie(token, jwtUtil.getExpirationSeconds());
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(AuthResponse.builder()
                .message("User registered successfully")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException(Constants.ERROR_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(Constants.ERROR_INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(user.getId());
        ResponseCookie cookie = buildAuthCookie(token, jwtUtil.getExpirationSeconds());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(AuthResponse.builder()
                .message("Login successful")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        ResponseCookie cookie = buildAuthCookie("", 0);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(AuthResponse.builder()
                .message("Logged out")
                .build());
    }

    private ResponseCookie buildAuthCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(Constants.AUTH_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite("Lax")
            .build();
    }
}

