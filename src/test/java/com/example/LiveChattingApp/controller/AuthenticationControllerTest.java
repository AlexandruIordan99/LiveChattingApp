package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.authentication.*;
import com.example.LiveChattingApp.security.JwtService;
import com.example.LiveChattingApp.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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

  @Test
  void authenticate_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
    AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
      .email("")
      .password("")
      .build();

    mockMvc.perform(post("/auth/authenticate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isBadRequest());

    verify(authenticationService, never()).authenticate(any(AuthenticationRequest.class));
  }

  @Test
  void authenticate_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
    AuthenticationRequest invalidEmailRequest = AuthenticationRequest.builder()
      .email("invalid-email")
      .password("password123")
      .build();

    mockMvc.perform(post("/auth/authenticate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
      .andExpect(status().isBadRequest());

    verify(authenticationService, never()).authenticate(any(AuthenticationRequest.class));
  }

  @Test
  void authenticate_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
      .thenThrow(new RuntimeException("Invalid credentials"));

    mockMvc.perform(post("/auth/authenticate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validAuthenticationRequest)))
      .andExpect(status().isInternalServerError());

    verify(authenticationService).authenticate(any(AuthenticationRequest.class));
  }

  @Test
  void activateAccount_ShouldReturnOk_WhenTokenIsValid() throws Exception {
    doNothing().when(authenticationService).activateAccount("valid-token");

    mockMvc.perform(get("/auth/activate-account")
        .param("token", "valid-token"))
      .andExpect(status().isOk());

    verify(authenticationService).activateAccount("valid-token");
  }

  @Test
  void activateAccount_ShouldReturnBadRequest_WhenTokenIsMissing() throws Exception {
    mockMvc.perform(get("/auth/activate-account"))
      .andExpect(status().isBadRequest());

    verify(authenticationService, never()).activateAccount(any());
  }

  @Test
  void activateAccount_ShouldReturnInternalServerError_WhenTokenIsInvalid() throws Exception {
    doThrow(new RuntimeException("Invalid token"))
      .when(authenticationService).activateAccount("invalid-token");

    mockMvc.perform(get("/auth/activate-account")
        .param("token", "invalid-token"))
      .andExpect(status().isInternalServerError());

    verify(authenticationService).activateAccount("invalid-token");
  }

  @Test
  void activateAccount_ShouldReturnInternalServerError_WhenTokenIsExpired() throws Exception {
    doThrow(new RuntimeException("Activation token has expired. A new token has been sent to your email address."))
      .when(authenticationService).activateAccount("expired-token");

    mockMvc.perform(get("/auth/activate-account")
        .param("token", "expired-token"))
      .andExpect(status().isInternalServerError());

    verify(authenticationService).activateAccount("expired-token");
  }

  @Test
  void activateAccount_ShouldReturnInternalServerError_WhenMessagingExceptionOccurs() throws Exception {
    doThrow(new MessagingException("Email service unavailable"))
      .when(authenticationService).activateAccount("valid-token");

    mockMvc.perform(get("/auth/activate-account")
        .param("token", "valid-token"))
      .andExpect(status().isInternalServerError());

    verify(authenticationService).activateAccount("valid-token");
  }

  @Test
  void register_ShouldHandleSpecialCharactersInRequest() throws Exception {
    RegistrationRequest specialCharsRequest = RegistrationRequest.builder()
      .firstname("José")
      .lastname("García-López")
      .email("jose.garcia@example.com")
      .password("pássword123!")
      .build();

    doNothing().when(authenticationService).register(any(RegistrationRequest.class));

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(specialCharsRequest)))
      .andExpect(status().isAccepted());

    verify(authenticationService).register(any(RegistrationRequest.class));
  }

  @Test
  void authenticate_ShouldHandleSpecialCharactersInRequest() throws Exception {
    AuthenticationRequest specialCharsRequest = AuthenticationRequest.builder()
      .email("josé.garcía@example.com")
      .password("pássword123!")
      .build();

    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
      .thenReturn(authenticationResponse);

    mockMvc.perform(post("/auth/authenticate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(specialCharsRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").value("jwt-token-123"));

    verify(authenticationService).authenticate(any(AuthenticationRequest.class));
  }

  @Test
  void activateAccount_ShouldHandleSpecialCharactersInToken() throws Exception {
    String tokenWithSpecialChars = "abc123!@#$%^&*()";
    doNothing().when(authenticationService).activateAccount(tokenWithSpecialChars);

    mockMvc.perform(get("/auth/activate-account")
        .param("token", tokenWithSpecialChars))
      .andExpect(status().isOk());

    verify(authenticationService).activateAccount(tokenWithSpecialChars);
  }

  @Test
  void register_ShouldReturnBadRequest_WhenContentTypeIsWrong() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.TEXT_PLAIN)
        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
      .andExpect(status().isUnsupportedMediaType());

    verify(authenticationService, never()).register(any(RegistrationRequest.class));
  }

}
