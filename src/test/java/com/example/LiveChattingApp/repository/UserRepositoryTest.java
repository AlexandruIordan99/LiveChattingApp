package com.example.LiveChattingApp.repository;

import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;

@DataJpaTest
@Testcontainers
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

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

  @Test
  public void UserRepository_CreateUser_ReturnSavedUser(){
    //Arrange
    User testUser = User.builder()
      .firstname("Alexandru")
      .lastname("Iordan")
      .displayName("Jordan299")
      .password("alunemari1234")
      .dateOfBirth("30.09.1999")
      .email("alexandru.iordan99@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(LocalDateTime.now())
      .build();

    //Act
    User savedUser = userRepository.save(testUser);
    //Assert
    Assertions.assertThat(savedUser).isNotNull();

  }



}
