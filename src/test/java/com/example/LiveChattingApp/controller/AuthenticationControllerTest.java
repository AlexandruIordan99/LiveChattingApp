package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.authentication.*;
import com.example.LiveChattingApp.security.JwtService;
import com.example.LiveChattingApp.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@Testcontainers
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthenticationService authenticationService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private RegistrationRequest validRegistrationRequest;
  private AuthenticationRequest validAuthenticationRequest;
  private AuthenticationResponse authenticationResponse;



  @BeforeEach
  void setUp() {
    validRegistrationRequest = RegistrationRequest.builder()
      .firstname("Alexandru")
      .lastname("Iordan")
      .email("alexandru.iordan99@gmail.com")
      .password("alunemari1234")
      .build();

    validAuthenticationRequest = AuthenticationRequest.builder()
      .email("alexandru.iordan99@gmail.com")
      .password("alunemari1234")
      .build();

    authenticationResponse = AuthenticationResponse.builder()
      .token("jwt-token-123")
      .build();
  }

  @Test
  void register_ShouldReturnAccepted_WhenRequestIsValid() throws Exception {
    doNothing().when(authenticationService).register(any(RegistrationRequest.class));

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
      .andExpect(status().isAccepted())
      .andExpect(content().string(""));

    verify(authenticationService).register(any(RegistrationRequest.class));
  }

  @Test
  void register_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
    RegistrationRequest invalidRequest = RegistrationRequest.builder()
      .firstname("")
      .lastname("")
      .email("invalid-email")
      .password("")
      .build();

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isBadRequest());

    verify(authenticationService, never()).register(any(RegistrationRequest.class));
  }


}
