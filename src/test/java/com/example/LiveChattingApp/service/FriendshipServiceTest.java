package com.example.LiveChattingApp.service;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
import com.example.LiveChattingApp.friendship.Friendship;
import com.example.LiveChattingApp.friendship.FriendshipRepository;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.friendship.FriendshipStatus;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
  private final LocalDateTime testTime = LocalDateTime.now();


  @BeforeEach
    void setUp(){
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

      user1.setId(1);

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
      user2.setId(2);

      user3 = User.builder()
        .firstname("Matei")
        .lastname("Paulet")
        .displayName("copilcoiot")
        .password("coiot9000")
        .dateOfBirth("17.07.1995")
        .email("mateipaulet1999@gmail.com")
        .accountLocked(false)
        .enabled(true)
        .createdDate(testTime)
        .build();
        user3.setId(3);

      friendship = new Friendship();
      friendship.setId(1);
      friendship.setUser(user1);
      friendship.setFriend(user2);
      friendship.setFriendshipsStatus(FriendshipStatus.PENDING);
      friendship.setCreatedAt(testTime);

    }


    @Test
    void testSendFriendRequestSuccess(){
      when(userRepository.findById(1)).thenReturn(Optional.of(user1));
      when(userRepository.findById(2)).thenReturn(Optional.of(user2));
      when(friendshipRepository.existsFriendshipBetweenUsers(1, 2)).thenReturn(false);
      when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);


      FriendshipResponseDTO result = friendshipService.sendFriendRequest(1, 2);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1);
      assertThat(result.getUserId()).isEqualTo(2);
      assertThat(result.getDisplayName()).isEqualTo("gtgmycatisonfire");
      assertThat(result.getEmail()).isEqualTo("vladloghin00@gmail.com");
      assertThat(result.getStatus()).isEqualTo("PENDING");
      assertThat(result.isRequester()).isTrue();

      verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void testSendFriendRequest_throwsException(){
      when(userRepository.findById(1)).thenReturn(Optional.of(user1));

      assertThatThrownBy(() -> friendshipService.sendFriendRequest(1, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A user cannot send a friend request to themselves.");

      verify(friendshipRepository, never()).save(any(Friendship.class));

    }

    @Test
    void testSendFriendRequest_UserNotFound_ThrowsException(){
      when(userRepository.findById(1)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> friendshipService.sendFriendRequest(1, 2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found with ID: 1");

      verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void acceptFriendRequest_Success(){
      when(friendshipRepository.findById(1)).thenReturn(Optional.of(friendship));
      when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

      FriendshipResponseDTO result = friendshipService.acceptFriendRequest(2, 1);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo("ACCEPTED");
      assertThat(result.isRequester()).isFalse();

      verify(friendshipRepository).save(argThat(f-> f.getFriendshipsStatus() == FriendshipStatus.ACCEPTED));

    }

  @Test
  void acceptFriendRequest_WrongRecipientException(){
    when(friendshipRepository.findById(1)).thenReturn(Optional.of(friendship));

    assertThatThrownBy(() -> friendshipService.acceptFriendRequest(3, 1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("You can only accept requests sent to you.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }

  @Test
  void acceptFriendRequest_IllegalArgumentException(){
    friendship.setFriendshipsStatus(FriendshipStatus.ACCEPTED);
    when(friendshipRepository.findById(1)).thenReturn(Optional.of(friendship));

    assertThatThrownBy(() -> friendshipService.acceptFriendRequest(2, 1))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("You can only accept pending requests.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }




}
