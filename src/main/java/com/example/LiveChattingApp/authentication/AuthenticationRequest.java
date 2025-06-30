package com.example.LiveChattingApp.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "Email is not correctly formatted")
    @NotEmpty(message= "Email name is mandatory")
    @NotBlank(message = "Email name is mandatory")
    private String email;

    @NotEmpty(message= "Password name is mandatory")
    @NotBlank(message = "Password name is mandatory")
    @Size(min= 8, message = "Password must be at least 8 characters.")
    private String password;

}
