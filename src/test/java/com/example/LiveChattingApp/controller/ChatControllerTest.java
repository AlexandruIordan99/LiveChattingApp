package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.message.MessageService;
import com.example.LiveChattingApp.security.UserSynchronizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureWebMvc
@SpringBootTest
public class ChatControllerTest {

  @MockitoBean
  private ChatService chatService;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MessageService messageService;

  @MockitoBean
  private UserSynchronizer userSynchronizer;



}
