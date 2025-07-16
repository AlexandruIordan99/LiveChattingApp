package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.messageRequest.MessageRequestRepository;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

@DataJpaTest
@Testcontainers
public class MessageRequestRepositoryTest {


  @Autowired
  private MessageRequestRepository messageRequestRepository;

  @Autowired
  private UserRepository userRepository;

  private User user1;
  private User user2;
  private User user3;

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

  }



}
