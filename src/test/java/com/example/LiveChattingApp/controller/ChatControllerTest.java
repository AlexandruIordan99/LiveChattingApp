package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.message.MessageService;
import com.example.LiveChattingApp.security.UserSynchronizer;
import com.example.LiveChattingApp.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureWebMvc
@SpringBootTest
public class ChatControllerTest {

  @MockitoBean
  private ChatService chatService;

  @Autowired
  private WebApplicationContext context;

  private ObjectMapper objectMapper;
  private User mockUser;
  private MockMvc mockMvc;

  @Autowired
  private MessageService messageService;

  @MockitoBean
  private UserSynchronizer userSynchronizer;

  @BeforeEach
  void setUp(){
    mockMvc = MockMvcBuilders
      .webAppContextSetup(context)
      .apply(springSecurity())
      .build();


    mockUser = User.builder()
      .id("1")
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();
  }


  @Test
  void test_createDirectChat(){
    //Arrange

    //Act & Assert

    //Verify
  }

  @Test
  void test_createGroupChat(){

    //Arrange

    //Act & Assert

    //Verify

  }

  @Test
  void test_addParticipant(){

    //Arrange

    //Act & Assert

    //Verify

  }
  @Test
  void test_removeParticipant(){

    //Arrange

    //Act & Assert

    //Verify

  }

  @Test
  void test_getChatParticipants(){

    //Arrange

    //Act & Assert

    //Verify

  }

  @Test
  void test_leaveChat(){

    //Arrange

    //Act & Assert

    //Verify

  }


}
