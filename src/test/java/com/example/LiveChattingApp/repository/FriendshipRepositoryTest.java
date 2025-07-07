package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.friendship.Friendship;
import com.example.LiveChattingApp.friendship.FriendshipRepository;
import com.example.LiveChattingApp.friendship.FriendshipStatus;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
public class FriendshipRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User user1;
  private User user2;
  private User user3;

  @Autowired
  private FriendshipRepository friendshipRepository;

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
    .withDatabaseName("testDB")
    .withUsername("postgres")
    .withPassword("pass");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry){
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

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
      .createdDate(LocalDateTime.now())
      .build();

    user2 = User.builder()
      .firstname("Vlad")
      .lastname("Loghin")
      .displayName("gtgmycatisonfire")
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
      .displayName("copilcoiot")
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
  void testFindFriendshipBetweenUsers_WhenFriendshipExists(){

    //Arrange
      Friendship friendship = Friendship.builder()
        .user(user1)
        .friend(user2)
        .friendshipsStatus(FriendshipStatus.ACCEPTED)
        .build();
      friendshipRepository.save(friendship);

      //Act
    Optional<Friendship> result = friendshipRepository.findFriendshipBetweenUsers(user1.getId(), user2.getId());

    //Assert
    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result.get().getUser().getId()).isEqualTo(user1.getId());
    Assertions.assertThat(result.get().getFriend().getId()).isEqualTo(user2.getId());

  }


  @Test
  void testFindFriendshipBetweenUsersReversed_WhenFriendshipExists(){

    //Arrange
    Friendship friendship = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();
    friendshipRepository.save(friendship);

    //Act
    Optional<Friendship> result = friendshipRepository.findFriendshipBetweenUsers(user2.getId(), user1.getId());

    //Assert
    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result.get().getUser().getId()).isEqualTo(user1.getId());
    Assertions.assertThat(result.get().getFriend().getId()).isEqualTo(user2.getId());

  }


  @Test
  void testFindFriendshipBetweenUsers_WhenFriendshipDoesNotExist() {
    // Act
    Optional<Friendship> result = friendshipRepository.findFriendshipBetweenUsers(user1.getId(), user2.getId());

    // Assert
    Assertions.assertThat(result).isEmpty();
  }


  @Test
  void testExistsFriendshipBetweenUsers_WhenFriendshipExists(){
    Friendship friendship = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();
    friendshipRepository.save(friendship);

    boolean exists = friendshipRepository.existsFriendshipBetweenUsers(user1.getId(), user2.getId());

    Assertions.assertThat(exists).isTrue();
  }

  @Test
  void testExistsFriendshipsBetweenUsers_whenFriendshipDoesNotExist(){
    boolean exists = friendshipRepository.existsFriendshipBetweenUsers(user1.getId(), user2.getId());

    Assertions.assertThat(exists).isFalse();
  }

  @Test
  void testFindAcceptedFriendships(){
    Friendship acceptedFriendship1 = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    Friendship acceptedFriendship2 = Friendship.builder()
      .user(user2)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    Friendship acceptedFriendship3 = Friendship.builder()
      .user(user1)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    friendshipRepository.saveAll(List.of(acceptedFriendship1, acceptedFriendship2, acceptedFriendship3));

    List<Friendship> acceptedFriendships = friendshipRepository.findAcceptedFriendships(user1.getId());

    Assertions.assertThat(acceptedFriendships).hasSize(1);
    Assertions.assertThat(acceptedFriendships)
      .extracting(Friendship::getFriendshipsStatus)
      .containsOnly(FriendshipStatus.ACCEPTED);
  }


  @Test
  void testFindPendingSentRequests(){
    Friendship pendingSentRequest1 = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    Friendship pendingSentRequest2 = Friendship.builder()
      .user(user1)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    Friendship acceptedFriendship = Friendship.builder()
      .user(user2)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    friendshipRepository.saveAll(List.of(pendingSentRequest1, pendingSentRequest2, acceptedFriendship));

    List<Friendship> pendingSentRequests = friendshipRepository.findPendingSentRequests(user1.getId());

    Assertions.assertThat(pendingSentRequests).hasSize(2);
    Assertions.assertThat(pendingSentRequests.getFirst().getUser().getId()).isEqualTo(user1.getId());
    Assertions.assertThat(pendingSentRequests.getFirst().getFriendshipsStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void testFindPendingReceivedRequests() {
    Friendship pendingReceivedRequest = Friendship.builder()
      .user(user2)
      .friend(user1)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    Friendship pendingSentRequest = Friendship.builder()
      .user(user1)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    friendshipRepository.saveAll(List.of(pendingReceivedRequest, pendingSentRequest));

    List<Friendship> pendingReceivedRequests = friendshipRepository.findPendingReceivedRequests(user1.getId());

    Assertions.assertThat(pendingReceivedRequests).hasSize(1);
    Assertions.assertThat(pendingReceivedRequests.getFirst().getFriend().getId()).isEqualTo(user1.getId());
    Assertions.assertThat(pendingReceivedRequests.getFirst().getFriendshipsStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void testFindBlockedUsers() {
    Friendship blockedUser = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.BLOCKED)
      .build();

    Friendship acceptedFriendship = Friendship.builder()
      .user(user1)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    friendshipRepository.saveAll(List.of(blockedUser, acceptedFriendship));

    List<Friendship> blockedUsers = friendshipRepository.findBlockedUsers(user1.getId());

    Assertions.assertThat(blockedUsers).hasSize(1);
    Assertions.assertThat(blockedUsers.getFirst().getUser().getId()).isEqualTo(user1.getId());
    Assertions.assertThat(blockedUsers.getFirst().getFriendshipsStatus()).isEqualTo(FriendshipStatus.BLOCKED);

  }

  @Test
  void testFindByUserIdAndFriendshipsStatus() {
    Friendship pendingFriendship = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    Friendship acceptedFriendship = Friendship.builder()
      .user(user1)
      .friend(user3)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    friendshipRepository.saveAll(List.of(pendingFriendship, acceptedFriendship));

    List<Friendship> pendingFriendships = friendshipRepository.findByUserIdAndFriendshipsStatus(user1.getId(), FriendshipStatus.PENDING);

    Assertions.assertThat(pendingFriendships).hasSize(1);
    Assertions.assertThat(pendingFriendships.getFirst().getFriendshipsStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void testFindByFriendIdAndFriendshipsStatus() {
    Friendship pendingFriendship = Friendship.builder()
      .user(user2)
      .friend(user1)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();

    Friendship acceptedFriendship = Friendship.builder()
      .user(user3)
      .friend(user1)
      .friendshipsStatus(FriendshipStatus.ACCEPTED)
      .build();

    friendshipRepository.saveAll(List.of(pendingFriendship, acceptedFriendship));

    List<Friendship> pendingReceivedRequests = friendshipRepository.findByFriendIdAndFriendshipsStatus(user1.getId(), FriendshipStatus.PENDING);

    Assertions.assertThat(pendingReceivedRequests).hasSize(1);
    Assertions.assertThat(pendingReceivedRequests.getFirst().getFriendshipsStatus()).isEqualTo(FriendshipStatus.PENDING);
  }

  @Test
  void testFindAcceptedFriendships_WhenNoAcceptedFriendships() {
    Friendship pendingFriendship = Friendship.builder()
      .user(user1)
      .friend(user2)
      .friendshipsStatus(FriendshipStatus.PENDING)
      .build();
    friendshipRepository.save(pendingFriendship);

    List<Friendship> acceptedFriendships = friendshipRepository.findAcceptedFriendships(user1.getId());

    Assertions.assertThat(acceptedFriendships).isEmpty();
  }

  @Test
  void testRepositoryMethodsWithNonExistentUser() {
    Integer nonExistentUserId = 999;

    Assertions.assertThat(friendshipRepository.findAcceptedFriendships(nonExistentUserId)).isEmpty();
    Assertions.assertThat(friendshipRepository.findPendingSentRequests(nonExistentUserId)).isEmpty();
    Assertions.assertThat(friendshipRepository.findPendingReceivedRequests(nonExistentUserId)).isEmpty();
    Assertions.assertThat(friendshipRepository.findBlockedUsers(nonExistentUserId)).isEmpty();
    Assertions.assertThat(friendshipRepository.existsFriendshipBetweenUsers(nonExistentUserId, user1.getId())).isFalse();
  }


}

