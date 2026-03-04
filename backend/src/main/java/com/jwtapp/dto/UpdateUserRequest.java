package com.jwtapp.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bio;
}
