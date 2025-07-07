package com.example.LiveChattingApp.service;

import com.example.LiveChattingApp.friendship.Friendship;
import com.example.LiveChattingApp.friendship.FriendshipRepository;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
public class FriendshipServiceTest {

  @Mock
  private FriendshipRepository friendshipRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private FriendshipService friendshipService;


  private User user1;
  private User user2;
  private User user3;
  private Friendship friendship;
  private LocalDateTime testTime;


  @BeforeEach
    void setUp(){
      user1 = User.builder()
        .firstname("Alexandru")
        .lastname("Iordan")
        .username("Jordan299")
        .password("alunemari1234")
        .dateOfBirth("30.11.1999")
        .email("alexandru.iordan99@gmail.com")
        .accountLocked(false)
        .enabled(true)
        .createdDate(LocalDateTime.now())
        .build();

      user2 = User.builder()
        .firstname("Vlad")
        .lastname("Loghin")
        .username("gtgmycatisonfire")
        .password("doomguy2000")
        .dateOfBirth("23.02.1996")
        .email("vladloghin00@gmail.com")
        .accountLocked(false)
        .enabled(true)
        .createdDate(LocalDateTime.now())
        .build();

      user3 = User.builder()
        .firstname("Matei")
        .lastname("Paulet")
        .username("copilcoiot")
        .password("coiot9000")
        .dateOfBirth("17.07.1995")
        .email("mateipaulet1999@gmail.com")
        .accountLocked(false)
        .enabled(true)
        .createdDate(LocalDateTime.now())
        .build();

      user1= userRepository.save(user1);
      user2 = userRepository.save(user2);
      user3 = userRepository.save(user3);
    }

    @Test
    void testSendFriendRequest(){
      when(userRepository.findById(1)).thenReturn(Optional.of(user1));

      assertThatThrownBy(() -> friendshipService.sendFriendRequest(1, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A user cannot send a friend request to themselves.");

      verify(friendshipRepository, never()).save(any(Friendship.class));

    }



}
