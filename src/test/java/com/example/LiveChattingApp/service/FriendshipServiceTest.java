package com.example.LiveChattingApp.service;

import com.example.LiveChattingApp.friendship.*;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

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
  void setUp() {
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
    user3.setId("3");

    friendship = new Friendship();
    friendship.setId(1L);
    friendship.setUser(user1);
    friendship.setFriend(user2);
    friendship.setFriendshipsStatus(FriendshipStatus.PENDING);
    friendship.setCreatedAt(testTime);

  }


  @Test
  void testSendFriendRequestSuccess() {
    when(userRepository.findById("1")).thenReturn(Optional.of(user1));
    when(userRepository.findById("2")).thenReturn(Optional.of(user2));
    when(friendshipRepository.existsFriendshipBetweenUsers("1", "2")).thenReturn(false);
    when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);


    FriendshipResponseDTO result = friendshipService.sendFriendRequest("1", "2");

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUserId()).isEqualTo("2");
    assertThat(result.getDisplayName()).isEqualTo("gtgmycatisonfire");
    assertThat(result.getEmail()).isEqualTo("vladloghin00@gmail.com");
    assertThat(result.getStatus()).isEqualTo("PENDING");
    assertThat(result.isRequester()).isTrue();

    verify(friendshipRepository).save(any(Friendship.class));
  }

  @Test
  void testSendFriendRequest_throwsException() {
    when(userRepository.findById("1")).thenReturn(Optional.of(user1));

    assertThatThrownBy(() -> friendshipService.sendFriendRequest("1", "1"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("A user cannot send a friend request to themselves.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }

  @Test
  void testSendFriendRequest_UserNotFound_ThrowsException() {
    when(userRepository.findById("1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> friendshipService.sendFriendRequest("1", "2"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("User not found with ID: 1");

    verify(friendshipRepository, never()).save(any(Friendship.class));
  }

  @Test
  void testAcceptFriendRequest_Success() {
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
    when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

    FriendshipResponseDTO result = friendshipService.acceptFriendRequest("2", 1L);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo("ACCEPTED");
    assertThat(result.isRequester()).isFalse();

    verify(friendshipRepository).save(argThat(f -> f.getFriendshipsStatus() == FriendshipStatus.ACCEPTED));

  }

  @Test
  void testAcceptFriendRequest_IllegalArgumentException() {
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

    assertThatThrownBy(() -> friendshipService.acceptFriendRequest("3", 1L))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("You can only accept requests sent to you.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }

  @Test
  void testAcceptFriendRequest_IllegalStateException() {
    friendship.setFriendshipsStatus(FriendshipStatus.ACCEPTED);
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

    assertThatThrownBy(() -> friendshipService.acceptFriendRequest("2", 1L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("You can only accept pending requests.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }

  @Test
  void testRejectFriendRequest() {
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
    when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

    assertThatCode(() -> friendshipService.rejectFriendRequest("2", 1L))
      .doesNotThrowAnyException();

    verify(friendshipRepository).save(argThat(f -> f.getFriendshipsStatus() == FriendshipStatus.REJECTED));
  }

  @Test
  void testRejectFriendRequest_IllegalArgumentException() {
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

    assertThatThrownBy(() -> friendshipService.rejectFriendRequest("3", 1L))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("You can only reject requests sent to you.");

    verify(friendshipRepository, never()).save(any(Friendship.class));

  }

  @Test
  void testRejectFriendRequest_IllegalStateException() {
    when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
    friendship.setFriendshipsStatus(FriendshipStatus.ACCEPTED);

    assertThatThrownBy(() -> friendshipService.rejectFriendRequest("2", 1L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("You can only reject pending requests.");

    verify(friendshipRepository, never()).save(any(Friendship.class));
  }

  @Test
  void removeFriend_Success() {
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.of(friendship));

    assertThatCode(() -> friendshipService.removeFriend("1", "2"))
      .doesNotThrowAnyException();

    verify(friendshipRepository).delete(friendship);
  }

  @Test
  void removeFriend_FriendshipNotFound_ThrowsException() {
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> friendshipService.removeFriend("1", "2"))
      .isInstanceOf(FriendshipNotFoundException.class)
      .hasMessage("Friendship not found between these users");

    verify(friendshipRepository, never()).delete(any(Friendship.class));
  }

  @Test
  void testBlockUser_ExistingFriendship_Success() {
    when(userRepository.findById("2")).thenReturn(Optional.of(user2));
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.of(friendship));
    when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

    assertThatCode(() -> friendshipService.blockUser("1", "2"))
      .doesNotThrowAnyException();

    verify(friendshipRepository).save(argThat(f ->
      f.getFriendshipsStatus() == FriendshipStatus.BLOCKED));
  }

  @Test
  void testBlockUser_NoExistingFriendship_Success() {
    when(userRepository.findById("2")).thenReturn(Optional.of(user2));
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.empty());
    when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

    assertThatCode(() -> friendshipService.blockUser("1", "2"))
      .doesNotThrowAnyException();

    verify(friendshipRepository).save(argThat(f ->
      f.getFriendshipsStatus() == FriendshipStatus.BLOCKED &&
        f.getFriend().getId().equals("2")));
  }


  @Test
  void testGetFriends_Success() {
    Friendship acceptedFriendship = new Friendship();
    acceptedFriendship.setId(1L);
    acceptedFriendship.setUser(user1);
    acceptedFriendship.setFriend(user2);
    acceptedFriendship.setFriendshipsStatus(FriendshipStatus.ACCEPTED);
    acceptedFriendship.setCreatedAt(testTime);

    when(friendshipRepository.findAcceptedFriendships("1"))
      .thenReturn(List.of(acceptedFriendship));

    List<FriendshipResponseDTO> result = friendshipService.getFriends("1");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo("2");
    assertThat(result.getFirst().getDisplayName()).isEqualTo("gtgmycatisonfire");
    assertThat(result.getFirst().getStatus()).isEqualTo("ACCEPTED");
    assertThat(result.getFirst().isRequester()).isTrue();
  }

  @Test
  void testGetReceivedPendingRequests_Success() {
    when(friendshipRepository.findPendingReceivedRequests("2"))
      .thenReturn(Collections.singletonList(friendship));

    List<FriendshipResponseDTO> result = friendshipService.getReceivedPendingRequests("2");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo("1");
    assertThat(result.getFirst().getDisplayName()).isEqualTo("Jordan299");
    assertThat(result.getFirst().getStatus()).isEqualTo("PENDING");
    assertThat(result.getFirst().isRequester()).isFalse();
  }

  @Test
  void testGetSentPendingRequests_Success() {
    when(friendshipRepository.findPendingSentRequests("1"))
      .thenReturn(Collections.singletonList(friendship));

    List<FriendshipResponseDTO> result = friendshipService.getSentPendingRequests("1");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo("2");
    assertThat(result.getFirst().getDisplayName()).isEqualTo("gtgmycatisonfire");
    assertThat(result.getFirst().getStatus()).isEqualTo("PENDING");
    assertThat(result.getFirst().isRequester()).isTrue();
  }

  @Test
  void testGetBlockedUsers_Success() {
    Friendship blockedFriendship = new Friendship();
    blockedFriendship.setId(1L);
    blockedFriendship.setUser(user1);
    blockedFriendship.setFriend(user2);
    blockedFriendship.setFriendshipsStatus(FriendshipStatus.BLOCKED);
    blockedFriendship.setCreatedAt(testTime);

    when(friendshipRepository.findBlockedUsers("1"))
      .thenReturn(List.of(blockedFriendship));

    List<FriendshipResponseDTO> result = friendshipService.getBlockedUsers("1");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo("2");
    assertThat(result.getFirst().getDisplayName()).isEqualTo("gtgmycatisonfire");
    assertThat(result.getFirst().getStatus()).isEqualTo("BLOCKED");
    assertThat(result.getFirst().isRequester()).isTrue();
  }

  @Test
  void testGetFriendshipStatus_ExistingFriendship_ReturnsStatus() {
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.of(friendship));

    String result = friendshipService.getFriendshipStatus("1", "2");

    assertThat(result).isEqualTo("PENDING");
  }

  @Test
  void testGetFriendshipStatus_NoFriendship_ReturnsNone() {
    when(friendshipRepository.findFriendshipBetweenUsers("1", "2"))
      .thenReturn(Optional.empty());

    String result = friendshipService.getFriendshipStatus("1", "2");

    assertThat(result).isEqualTo("NONE");
  }

  @Test
  void testFindFriendshipById_NotFound_ThrowsException() {
    when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> friendshipService.acceptFriendRequest("2", 999L))
      .isInstanceOf(FriendshipNotFoundException.class)
      .hasMessage("Friendship not found with ID: 999");
  }

  @Test
  void testMapToResponseDto_CurrentUserIsFriend_MapsCorrectly() {
    when(friendshipRepository.findAcceptedFriendships("2"))
      .thenReturn(Collections.singletonList(friendship));

    List<FriendshipResponseDTO> result = friendshipService.getFriends("2");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo("1");
    assertThat(result.getFirst().getDisplayName()).isEqualTo("Jordan299");
    assertThat(result.getFirst().isRequester()).isFalse();
  }

  @Test
  void testGetFriends_NoFriends_ReturnsEmptyList() {
    when(friendshipRepository.findAcceptedFriendships("1"))
      .thenReturn(List.of());

    List<FriendshipResponseDTO> result = friendshipService.getFriends("1");

    assertThat(result).isEmpty();
  }

}