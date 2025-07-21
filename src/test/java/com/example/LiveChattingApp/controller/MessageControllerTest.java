package com.example.LiveChattingApp.controller;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.message.*;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
@ActiveProfiles(value="dev")
@SpringBootTest
public class MessageControllerTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockitoBean
  private MessageService messageService;

  private ObjectMapper objectMapper;
  private User mockUser;

  @Autowired
  private MessageMapper mapper;

  @Qualifier("springSecurityFilterChain")
  @Autowired
  private Filter springSecurityFilterChain;
  @Autowired
  private MessageController messageController;

  @MockitoBean
  private UserRepository userRepository;

  @BeforeEach
  void setUp(){
    mockMvc = MockMvcBuilders
      .webAppContextSetup(context)
      .apply(springSecurity())
      .build();


    Chat directChat = Chat.builder()
      .id(1L)
      .type(ChatType.DIRECT)
    .build();

    Chat groupChat = Chat.builder()
      .id(2L)
      .type(ChatType.GROUP)
      .build();


    mockUser = User.builder()
      .id(String.valueOf(1))
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();


  }


  @Test
  void test_sendDirectMessage_when() throws Exception {
    //Arrange
    MessageInputDTO messageInputDTO = new MessageInputDTO();

    doNothing().when(messageService).sendDirectMessage(any(), any(), any(), any());
    when(messageService.getMessageReceiverId(any(), any())).thenReturn("2");
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));

    //Act & Assert
    mockMvc.perform(post("/messages/direct-chats/1")
      .with(authentication
        (new MockAuthenticationUtils.MockAuthentication(mockUser)))
        .contentType(MediaType.APPLICATION_JSON)
      .content("""
              {"content" : "hiii"}
              \s""")
    ).andExpect(status().isOk());


    //Verify
    verify(messageService,times(1)).sendDirectMessage(
      argThat(userId -> userId.equals("1")),
      argThat(receiverId -> receiverId.equals("2")),
      argThat(chatId -> chatId == 1L),
      argThat(content -> content.equals("hiii")))
    ;

  }

  @Test
  void test_sendGroupMessage() throws Exception{
    //Arrange
    MessageInputDTO messageInputDTO = new MessageInputDTO();

    doNothing().when(messageService).sendGroupMessage(any(), any(), any());
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));
    //Act & Assert
    mockMvc.perform(post("/messages/group-chats/2")
      .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser)))
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
          {"content":  "hiii"}
        """)).andExpect(status().isOk());

    //Verify
    verify(messageService, times(1)).sendGroupMessage(
      argThat(userId -> userId.equals("1")),
        argThat(chatId -> chatId == 2L),
      argThat(messageInputDTOContent -> messageInputDTOContent.equals("hiii")));

  }

}



