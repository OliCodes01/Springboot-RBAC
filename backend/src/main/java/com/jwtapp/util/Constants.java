package com.jwtapp.util;

public final class Constants {

    private Constants() {}

    public static final String AUTH_COOKIE_NAME = "jwt";

    public static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
    public static final String PUBLIC_KEY_PATH = "keys/public_key.pem";

    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_USERNAME_EXISTS = "Username already exists";
    public static final String ERROR_EMAIL_EXISTS = "Email already exists";
    public static final String ERROR_ADMIN_REQUIRED = "Access denied: Admin role required";
    public static final String ERROR_CURRENT_PASSWORD_INCORRECT = "Current password is incorrect";
    public static final String ERROR_NEW_PASSWORD_TOO_SHORT = "New password must be at least 6 characters long";
}
