package com.example.LiveChattingApp.controller;


import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.message.MessageController;
import com.example.LiveChattingApp.message.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureWebMvc
@WebMvcTest(MessageController.class)
public class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChatService chatService;

  @Autowired
  private MessageMapper mapper;

}
