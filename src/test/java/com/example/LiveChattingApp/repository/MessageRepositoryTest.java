package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageRepository;
import com.example.LiveChattingApp.message.MessageState;
import com.example.LiveChattingApp.message.MessageType;
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
import java.util.Set;

@DataJpaTest
@Testcontainers
public class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

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
  void test_findMessagesByChatId(){
    Message message1 = Message.builder()
      .content("Heyoo")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .createdDate(LocalDateTime.now().minusMinutes(5))
      .build();

    Message message2 = Message.builder()
      .content("Helloo")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .createdDate(LocalDateTime.now().minusMinutes(3))
      .build();

    Message message3 = Message.builder()
      .content("Helloo")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .createdDate(LocalDateTime.now())
      .build();

    Message messageInDifferentChat = Message.builder()
      .content("Imposter here")
      .sender(user3)
      .chat(groupChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .createdDate(LocalDateTime.now())
      .build();

    messageRepository.save(message1);
    messageRepository.save(message2);
    messageRepository.save(message3);
    messageRepository.save(messageInDifferentChat);

    // Act
    List<Message> messages = messageRepository.findMessagesByChatId(directChat.getId());

    // Assert
    Assertions.assertThat(messages)
      .hasSize(3)
      .containsExactly(message1, message2, message3);

  }

  @Test
  void test_countUnreadMessagesByChatAndUserId(){

    // Arrange
    Message readMessage = Message.builder()
      .content("Read message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.READ)
      .build();

    Message unreadMessage1 = Message.builder()
      .content("Unread message 1")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    Message unreadMessage2 = Message.builder()
      .content("Unread message 2")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.DELIVERED)
      .build();

    Message messageFromUser2 = Message.builder()
      .content("Message from user2")
      .sender(user2)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    messageRepository.save(readMessage);
    messageRepository.save(unreadMessage1);
    messageRepository.save(unreadMessage2);
    messageRepository.save(messageFromUser2);

    // Act
    long unreadCount = messageRepository.countUnreadMessagesByChatIdAndUserId(
      directChat.getId(), user2.getId());

    // Assert
    Assertions.assertThat(unreadCount).isEqualTo(2);

  }

  @Test
  void test_findReadMessagesByMessageId(){
    // Arrange
    Message readMessage = Message.builder()
      .content("Read message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.READ)
      .build();

    Message unreadMessage = Message.builder()
      .content("Unread message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    readMessage = messageRepository.save(readMessage);
    unreadMessage = messageRepository.save(unreadMessage);

    // Act
    List<Message> readMessages = messageRepository.findReadMessagesByMessageId(readMessage.getId());
    List<Message> unreadMessages = messageRepository.findReadMessagesByMessageId(unreadMessage.getId());

    // Assert
    Assertions.assertThat(readMessages)
      .hasSize(1)
      .containsExactly(readMessage);

    Assertions.assertThat(unreadMessages).isEmpty();

  }

  @Test
  void test_markMessagesAsRead(){
    // Arrange
    Message sentMessage = Message.builder()
      .content("Sent message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    Message deliveredMessage = Message.builder()
      .content("Delivered message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.DELIVERED)
      .build();

    Message alreadyReadMessage = Message.builder()
      .content("Already read message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.READ)
      .build();

    Message sentMessageFromUser2 = Message.builder()
      .content("Message from user2")
      .sender(user2)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    sentMessage = messageRepository.save(sentMessage);
    deliveredMessage = messageRepository.save(deliveredMessage);
    alreadyReadMessage = messageRepository.save(alreadyReadMessage);
    sentMessageFromUser2 = messageRepository.save(sentMessageFromUser2);

    // Act
    messageRepository.markMessagesAsRead(directChat.getId(), user2.getId(), MessageState.READ);

    // Assert
    Message updatedSentMessage = messageRepository.findById(sentMessage.getId()).orElseThrow();
    Message updatedDeliveredMessage = messageRepository.findById(deliveredMessage.getId()).orElseThrow();
    Message updatedAlreadyReadMessage = messageRepository.findById(alreadyReadMessage.getId()).orElseThrow();
    Message updatedMessageFromUser2 = messageRepository.findById(sentMessageFromUser2.getId()).orElseThrow();

    Assertions.assertThat(updatedSentMessage.getState()).isEqualTo(MessageState.READ);
    Assertions.assertThat(updatedDeliveredMessage.getState()).isEqualTo(MessageState.READ);
    Assertions.assertThat(updatedAlreadyReadMessage.getState()).isEqualTo(MessageState.READ);
    Assertions.assertThat(updatedMessageFromUser2.getState()).isEqualTo(MessageState.SENT);
  }

  @Test
  void test_findMessagesByChatId_emptyChat(){
    // Act
    List<Message> messages = messageRepository.findMessagesByChatId(directChat.getId());

    // Assert
    Assertions.assertThat(messages).isEmpty();
  }

  @Test
  void test_countUnreadMessagesByChatAndUserId_noUnreadMessages(){
    // Arrange
    Message readMessage = Message.builder()
      .content("Read message")
      .sender(user1)
      .chat(directChat)
      .type(MessageType.TEXT)
      .state(MessageState.READ)
      .build();

    messageRepository.save(readMessage);

    // Act
    long unreadCount = messageRepository.countUnreadMessagesByChatIdAndUserId(
      directChat.getId(), user2.getId());

    // Assert
    Assertions.assertThat(unreadCount).isEqualTo(0);
  }


}
