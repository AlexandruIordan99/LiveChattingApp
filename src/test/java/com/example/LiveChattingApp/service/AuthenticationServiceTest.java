package com.example.LiveChattingApp.service;

import com.example.LiveChattingApp.authentication.AuthenticationRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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



}
