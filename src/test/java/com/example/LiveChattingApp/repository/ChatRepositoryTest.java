package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@DataJpaTest
@Testcontainers
public class ChatRepositoryTest {

  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private UserRepository userRepository;

  private User user1;
  private User user2;
  private User user3;

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
   void test_doesDirectChatExist(){

    //Arrange
    Chat chat = Chat.builder()
      .participants(Set.of(user1, user2))
      .type(ChatType.DIRECT)
      .build();

    chatRepository.save(chat);

    //Act

    Optional<Chat> directChat = chatRepository.findDirectChatBetweenUsers(user1.getId(), user2.getId());

    //Assert
    Assertions.assertThat(directChat).isPresent();
    Assertions.assertThat(directChat.get().getParticipants()).containsExactlyInAnyOrder(user1, user2);

  }

  @Test
  void test_canFindChats(){
    //Arrange
    Chat directChat = Chat.builder()
      .participants(Set.of(user1, user2))
      .type(ChatType.DIRECT)
      .build();

    Chat groupChat =  Chat.builder()
      .participants(Set.of(user1, user2, user3))
      .type(ChatType.GROUP)
      .build();

    chatRepository.save(directChat);
    chatRepository.save(groupChat);

    //Act
    List<Chat> user1Chats = chatRepository.findChatsByUserId(user1.getId());

    //Assert
    Assertions.assertThat(user1Chats).containsExactlyInAnyOrder(directChat, groupChat);
  }

  @Test
  void test_ChatsWithMoreThan2ParticipantsAreGroupChats(){
    //Arrange
    Chat lyingGroupChat = Chat.builder()
      .participants(Set.of(user1, user2, user3))
      .type(ChatType.DIRECT)
      .build();

    //Act & Assert
    Assertions.assertThatThrownBy(() -> chatRepository.save(lyingGroupChat))
      .isInstanceOf(InvalidDataAccessApiUsageException.class)
      .hasMessage("Direct chats must have exactly 2 participants.")
      .hasCauseInstanceOf(IllegalArgumentException.class);

  }

  @Test
  void test_ChatsWithLessThan3ParticipantsAreDirectChats(){
    //Arrange
    Chat lyingDirectChat = Chat.builder()
      .participants(Set.of(user1, user2))
      .type(ChatType.GROUP)
      .build();

    //Act & Assert
    Assertions.assertThatThrownBy(() -> chatRepository.save(lyingDirectChat))
      .isInstanceOf(InvalidDataAccessApiUsageException.class)
      .hasMessage("Group chats must have at least 3 participants.")
      .hasCauseInstanceOf(IllegalArgumentException.class);


  }

}
