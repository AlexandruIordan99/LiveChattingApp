package com.example.LiveChattingApp.service;

import com.example.LiveChattingApp.authentication.AuthenticationRequest;
import com.example.LiveChattingApp.authentication.AuthenticationResponse;
import com.example.LiveChattingApp.authentication.AuthenticationService;
import com.example.LiveChattingApp.authentication.RegistrationRequest;
import com.example.LiveChattingApp.email.EmailService;
import com.example.LiveChattingApp.email.EmailTemplateName;
import com.example.LiveChattingApp.role.Role;
import com.example.LiveChattingApp.role.RoleRepository;
import com.example.LiveChattingApp.security.JwtService;
import com.example.LiveChattingApp.user.Token;
import com.example.LiveChattingApp.user.TokenRepository;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@Testcontainers
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private EmailService emailService;

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthenticationService authenticationService;

  private Role userRole;
  private User user;
  private RegistrationRequest registrationRequest;
  private AuthenticationRequest authenticationRequest;
  private Token validToken;


  @BeforeEach
  void setUp(){
    userRole = Role.builder()
      .id(1)
      .name("USER")
      .build();

    user = User.builder()
      .id(1)
      .firstname("Alexandru")
      .lastname("Iordan")
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .password("alunemari1234")
      .accountLocked(false)
      .enabled(true)
      .createdDate(LocalDateTime.now())
      .roles(List.of(userRole))
      .build();

    registrationRequest = RegistrationRequest.builder()
      .firstname("Alexandru")
      .lastname("Iordan")
      .email("alexandru.iordan99@gmail.com")
      .password("alunemari1234")
      .build();

    authenticationRequest = AuthenticationRequest.builder()
      .email("alexandru.iordan99@gmail.com")
      .password("alunemari1234")
      .build();

    validToken = Token.builder()
      .id(1)
      .token("123456")
      .createdAt(LocalDateTime.now())
      .expiresAt(LocalDateTime.now().plusMinutes(15))
      .user(user)
      .build();

    ReflectionTestUtils.setField(authenticationService, "activationUrl", "http://localhost:8090/activate");
  }

  @Test
  void test_registerCreatesAndSendsValidationEmail() throws MessagingException {

    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode("alunemari1234")).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(tokenRepository.save(any(Token.class))).thenReturn(validToken);

    authenticationService.register(registrationRequest);


    verify(roleRepository).findByName("USER");
    verify(passwordEncoder).encode("alunemari1234");
    verify(userRepository).save(argThat(user ->
      user.getFirstname().equals("Alexandru") &&
      user.getLastname().equals("Iordan") &&
        user.getEmail().equals("alexandru.iordan99@gmail.com") &&
        user.getPassword().equals("encodedPassword") &&
        !user.isAccountLocked() &&
        !user.isEnabled() &&
        user.getRoles().contains(userRole)
    ));
    verify(tokenRepository).save(any(Token.class));
    verify(emailService).sendEmail(
      eq("alexandru.iordan99@gmail.com"),
      eq("Alexandru Iordan"),
      eq(EmailTemplateName.ACTIVATE_ACCOUNT),
      eq("http://localhost:8090/activate"),
      anyString(),
      eq("Account activation")
    );
  }

  @Test
  void register_ShouldThrowException_WhenUserRoleNotFound() throws MessagingException {
    // Given
    when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

    // When & Then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      authenticationService.register(registrationRequest);
    });

    assertEquals("Role USER not initialized.", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
    verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyString(), anyString(), anyString());
  }

  @Test
  void authenticate_ShouldReturnAuthenticationResponse() {
    Authentication mockAuth = mock(Authentication.class);
    when(mockAuth.getPrincipal()).thenReturn(user);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
      .thenReturn(mockAuth);
    when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("jwt-token");

    AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    verify(authenticationManager).authenticate(argThat(token ->
      token.getPrincipal().equals("alexandru.iordan99@gmail.com") &&
        token.getCredentials().equals("alunemari1234")
    ));
    verify(jwtService).generateToken(argThat(claims ->
      claims.get("fullName").equals("Alexandru Iordan")
    ), eq(user));
  }

  @Test
  void activateAccount_ShouldActivateUser_WhenTokenIsValid() throws MessagingException {
    when(tokenRepository.findByToken("123456")).thenReturn(Optional.of(validToken));
    when(userRepository.findById(1)).thenReturn(Optional.of(user));

    authenticationService.activateAccount("123456");

    verify(tokenRepository).findByToken("123456");
    verify(userRepository).findById(1);
    verify(userRepository).save(argThat(user -> user.isEnabled()));
    verify(tokenRepository).save(argThat(token -> token.getValidatedAt() != null));
  }

  @Test
  void activateAccount_ShouldThrowException_WhenTokenNotFound() {
    when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      authenticationService.activateAccount("invalid-token");
    });

    assertEquals("Invalid token", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void activateAccount_ShouldSendNewTokenAndThrowException_WhenTokenIsExpired() throws MessagingException {
    Token expiredToken = Token.builder()
      .id(1)
      .token("expired-token")
      .createdAt(LocalDateTime.now().minusMinutes(20))
      .expiresAt(LocalDateTime.now().minusMinutes(5))
      .user(user)
      .build();

    when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
    when(tokenRepository.save(any(Token.class))).thenReturn(validToken);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      authenticationService.activateAccount("expired-token");
    });

    assertTrue(exception.getMessage().contains("Activation token has expired"));
    verify(emailService).sendEmail(
      eq("alexandru.iordan99@gmail.com"),
      eq("Alexandru Iordan"),
      eq(EmailTemplateName.ACTIVATE_ACCOUNT),
      eq("http://localhost:8090/activate"),
      anyString(),
      eq("Account activation")
    );
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void activateAccount_ShouldThrowException_WhenUserNotFound() {
    when(tokenRepository.findByToken("123456")).thenReturn(Optional.of(validToken));
    when(userRepository.findById(1)).thenReturn(Optional.empty());

    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
      authenticationService.activateAccount("123456");
    });

    assertEquals("User not found", exception.getMessage());
    verify(tokenRepository, never()).save(any(Token.class));
  }

}
