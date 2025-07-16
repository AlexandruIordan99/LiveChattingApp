package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.messageRequest.MessageRequestRepository;
import com.example.LiveChattingApp.messageRequest.MessageRequestStatus;
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
import java.util.Optional;
import java.util.Set;

@DataJpaTest
@Testcontainers
public class MessageRequestRepositoryTest {


  @Autowired
  private MessageRequestRepository messageRequestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRepository chatRepository;
  
  private User user1;
  private User user2;
  private User user3;
  private Chat directChat;
  private Chat groupChat;

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
    .withDatabaseName("testDB")
    .withUsername("postgres")
    .withPassword("password");


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

      directChat = Chat.builder()
      .participants(Set.of(user1, user2))
      .type(ChatType.DIRECT)
      .build();

     groupChat = Chat.builder()
      .participants(Set.of(user1, user2, user3))
      .type(ChatType.GROUP)
      .build();

    directChat = chatRepository.save(directChat);
    groupChat = chatRepository.save(groupChat);
  }


  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_findsPendingRequest(){
    // Arrange
    MessageRequest pendingRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.PENDING)
      .chat(directChat)
      .build();

    messageRequestRepository.save(pendingRequest);

    // Act
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.PENDING);

    // Assert
    Assertions.assertThat(foundRequest).isPresent();
    Assertions.assertThat(foundRequest.get().getSenderId()).isEqualTo(user1.getId());
    Assertions.assertThat(foundRequest.get().getReceiverId()).isEqualTo(user2.getId());
    Assertions.assertThat(foundRequest.get().getStatus()).isEqualTo(MessageRequestStatus.PENDING);
  }

  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_findsAcceptedRequest(){
    // Arrange
    MessageRequest acceptedRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.ACCEPTED)
      .chat(directChat)
      .build();

    messageRequestRepository.save(acceptedRequest);

    // Act
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.ACCEPTED);

    // Assert
    Assertions.assertThat(foundRequest).isPresent();
    Assertions.assertThat(foundRequest.get().getStatus()).isEqualTo(MessageRequestStatus.ACCEPTED);
  }

  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_findsRejectedRequest(){
    // Arrange
    MessageRequest rejectedRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.DECLINED)
      .chat(directChat)
      .build();

    messageRequestRepository.save(rejectedRequest);

    // Act
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.DECLINED);

    // Assert
    Assertions.assertThat(foundRequest).isPresent();
    Assertions.assertThat(foundRequest.get().getStatus()).isEqualTo(MessageRequestStatus.DECLINED);
  }

  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_findsReverseRequest(){
    // Arrange
    MessageRequest pendingRequest = MessageRequest.builder()
      .senderId(user2.getId())
      .receiverId(user1.getId())
      .status(MessageRequestStatus.PENDING)
      .chat(directChat)
      .build();

    messageRequestRepository.save(pendingRequest);

    // Act - Search with user1 as sender and user2 as receiver
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.PENDING);

    // Assert - Should find the request where user2 is sender and user1 is receiver
    Assertions.assertThat(foundRequest).isPresent();
    Assertions.assertThat(foundRequest.get().getSenderId()).isEqualTo(user2.getId());
    Assertions.assertThat(foundRequest.get().getReceiverId()).isEqualTo(user1.getId());
    Assertions.assertThat(foundRequest.get().getStatus()).isEqualTo(MessageRequestStatus.PENDING);
  }

  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_doesNotFindDifferentStatus(){
    // Arrange
    MessageRequest acceptedRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.ACCEPTED)
      .chat(directChat)
      .build();

    messageRequestRepository.save(acceptedRequest);

    // Act - Search for PENDING status
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.PENDING);

    // Assert - Should not find the ACCEPTED request
    Assertions.assertThat(foundRequest).isNotPresent();
  }


  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_doesNotFindDifferentUsers(){
    // Arrange
    MessageRequest pendingRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.PENDING)
      .chat(directChat)
      .build();

    messageRequestRepository.save(pendingRequest);

    // Act - Search for different users
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user3.getId(), MessageRequestStatus.PENDING);

    // Assert - Should not find the request
    Assertions.assertThat(foundRequest).isNotPresent();
  }

  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_multipleRequestsOnlyFindsCorrectOne(){
    // Arrange
    MessageRequest pendingRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.PENDING)
      .chat(directChat)
      .build();

    MessageRequest rejectedRequest = MessageRequest.builder()
      .senderId(user1.getId())
      .receiverId(user2.getId())
      .status(MessageRequestStatus.DECLINED)
      .chat(directChat)
      .build();

    MessageRequest differentUsersRequest = MessageRequest.builder()
      .senderId(user2.getId())
      .receiverId(user3.getId())
      .status(MessageRequestStatus.PENDING)
      .chat(directChat)
      .build();

    messageRequestRepository.save(pendingRequest);
    messageRequestRepository.save(rejectedRequest);
    messageRequestRepository.save(differentUsersRequest);

    // Act
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.PENDING);

    // Assert
    Assertions.assertThat(foundRequest).isPresent();
    Assertions.assertThat(foundRequest.get().getId()).isEqualTo(pendingRequest.getId());
    Assertions.assertThat(foundRequest.get().getStatus()).isEqualTo(MessageRequestStatus.PENDING);
  }


  @Test
  void test_findBySenderIdAndReceiverIdAndMessageRequestStatus_noRequestsFound(){
    // Arrange - no requests saved

    // Act
    Optional<MessageRequest> foundRequest = messageRequestRepository
      .findBySenderIdAndReceiverIdAndMessageRequestStatus(
        user1.getId(), user2.getId(), MessageRequestStatus.PENDING);

    // Assert
    Assertions.assertThat(foundRequest).isNotPresent();
  }


}
