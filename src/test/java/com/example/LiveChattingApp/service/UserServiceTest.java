package com.example.LiveChattingApp.service;


import com.example.LiveChattingApp.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Mock
  private UserMapper userMapper;

  @Mock
  private Authentication authentication;

  private User user1;
  private User user2;
  private UserResponse userResponse1;
  private UserResponse userResponse2;
  private UserMapper mapper;
  private LocalDateTime testTime;

  @BeforeEach
  void setUp(){
    testTime =  LocalDateTime.now();

    user1 = User.builder()
      .firstname("Alexandru")
      .lastname("Iordan")
      .displayName("Jordan299")
      .password("alunemari1234")
      .dateOfBirth("30.11.1999")
      .email("alexandru.iordan99@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(testTime)
      .build();

    user1.setId("1");

    user2 = User.builder()
      .firstname("Vlad")
      .lastname("Loghin")
      .displayName("gtgmycatisonfire")
      .password("doomguy2000")
      .dateOfBirth("23.02.1996")
      .email("vladloghin00@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(testTime)
      .build();
    user2.setId("2");

    userResponse1 = UserResponse.builder()
      .id("1")
      .firstName("Alexandru")
      .lastName("Iordan")
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .lastSeen(testTime)
      .isOnline(true)
      .build();

    userResponse2 = UserResponse.builder()
      .id("2")
      .firstName("Vlad")
      .lastName("Loghin")
      .displayName("gtgmycatisonfire")
      .email("vladloghin00@gmail.com")
      .lastSeen(testTime)
      .isOnline(false)
      .build();
  }


  @Test
  void getAllUsersExceptSelf_ShouldReturnMappedUserResponses_WhenUsersExist() {
    // Arrange
    String currentUserName = "Jordan299";
    List<User> users = List.of(user2);

    when(authentication.getName()).thenReturn(currentUserName);
    when(userRepository.findAllUsersExceptSelf(currentUserName)).thenReturn(users);
    when(userMapper.toUserResponse(user2)).thenReturn(userResponse2);

    // Act
    List<UserResponse> result = userService.getAllUsersExceptSelf(authentication);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(userResponse2);

    verify(userRepository).findAllUsersExceptSelf(currentUserName);
    verify(userMapper).toUserResponse(user2);
  }

  @Test
  void getAllUsersExceptSelf_ShouldReturnEmptyList_WhenNoOtherUsersExist() {
    // Arrange
    String currentUserName = "Jordan299";
    List<User> emptyUserList = List.of();

    when(authentication.getName()).thenReturn(currentUserName);
    when(userRepository.findAllUsersExceptSelf(currentUserName)).thenReturn(emptyUserList);

    // Act
    List<UserResponse> result = userService.getAllUsersExceptSelf(authentication);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findAllUsersExceptSelf(currentUserName);
    verify(userMapper, never()).toUserResponse(any());
  }

  @Test
  void getAllUsersExceptSelf_ShouldReturnMultipleUsers_WhenMultipleUsersExist() {
    // Arrange
    String currentUserName = "someOtherUser";
    List<User> users = List.of(user1, user2);

    when(authentication.getName()).thenReturn(currentUserName);
    when(userRepository.findAllUsersExceptSelf(currentUserName)).thenReturn(users);
    when(userMapper.toUserResponse(user1)).thenReturn(userResponse1);
    when(userMapper.toUserResponse(user2)).thenReturn(userResponse2);

    // Act
    List<UserResponse> result = userService.getAllUsersExceptSelf(authentication);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(userResponse1, userResponse2);

    verify(userRepository).findAllUsersExceptSelf(currentUserName);
    verify(userMapper).toUserResponse(user1);
    verify(userMapper).toUserResponse(user2);
  }

  @Test
  void findById_ShouldReturnUser_WhenUserExists() {
    // Arrange
    String userId = "1";
    when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

    // Act
    Optional<User> result = userService.findById(userId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(user1);

    verify(userRepository).findById(userId);
  }

  @Test
  void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
    // Arrange
    String userId = "999";
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userService.findById(userId);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findById(userId);
  }

  @Test
  void findById_ShouldHandleNullUserId() {
    // Arrange
    String userId = null;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userService.findById(userId);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findById(userId);
  }

  @Test
  void findByDisplayName_ShouldReturnUser_WhenUserExists() {
    // Arrange
    String displayName = "Jordan299";
    when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.of(user1));

    // Act
    Optional<User> result = userService.findByDisplayName(displayName);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(user1);
    assertThat(result.get().getDisplayName()).isEqualTo(displayName);

    verify(userRepository).findByDisplayName(displayName);
  }

  @Test
  void findByDisplayName_ShouldReturnEmpty_WhenUserDoesNotExist() {
    // Arrange
    String displayName = "nonExistentUser";
    when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userService.findByDisplayName(displayName);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findByDisplayName(displayName);
  }

  @Test
  void findByDisplayName_ShouldHandleNullDisplayName() {
    // Arrange
    String displayName = null;
    when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userService.findByDisplayName(displayName);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findByDisplayName(displayName);
  }

  @Test
  void findByDisplayName_ShouldHandleEmptyDisplayName() {
    // Arrange
    String displayName = "";
    when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userService.findByDisplayName(displayName);

    // Assert
    assertThat(result).isEmpty();

    verify(userRepository).findByDisplayName(displayName);
  }





}
