package com.example.LiveChattingApp.controller;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.message.*;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
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

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ChatRepository chatRepository;


  @BeforeEach
  void setUp() {
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
      .id(1L)
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();

  }


  @Test
  void test_sendDirectMessage_when() throws Exception {
    //Arrange
    MessageInputDTO messageInputDTO = new MessageInputDTO();

    doNothing().when(messageService).sendDirectMessage(any(), any(), any(), any());
    when(messageService.getMessageReceiverId(any(), any())).thenReturn(2L);
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser));

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
      argThat(userId -> userId.equals(1L)),
      argThat(receiverId -> receiverId.equals(2L)),
      argThat(chatId -> chatId == 1L),
      argThat(content -> content.equals("hiii")))
    ;

  }

  @Test
  void test_sendGroupMessage() throws Exception{
    //Arrange
    MessageInputDTO messageInputDTO = new MessageInputDTO();

    doNothing().when(messageService).sendGroupMessage(any(), any(), any());
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser));
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


  @Test
  void test_getChatMessages() throws Exception {
    //Arrange
    MessageResponse messageResponse = Mockito.mock(MessageResponse.class);
    Authentication auth = Mockito.mock(Authentication.class);
    when(messageService.findChatMessages(1L, 1L)).thenReturn(List.of(messageResponse));
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));

    //Act & Assert
    mockMvc.perform(get("/messages/chat/1")
      .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser)))
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    //Verify
    verify(messageService, times(1)).findChatMessages(
      argThat(chatId -> chatId ==1L),
      argThat(senderId -> senderId.equals("1")
    ));

  }

  @Test
  @WithMockUser(username= "alexandru.iordan99@gmail.com")
  void test_markMessagesAsRead() throws Exception {
    //Arrange
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));
    doNothing().when(messageService).markAsRead(1L, 1L);

    //Act & Assert
    mockMvc.perform(post("/messages/chat/1/read")
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    //Verify
    verify(messageService, times(1)).markAsRead(
      argThat(chatId -> chatId == 1L),
      argThat(senderId -> senderId.equals(1L))
    );

  }

  @Test
  @WithMockUser(username= "alexandru.iordan99@gmail.com")
  void test_uploadMediaMessage() throws Exception {
    //Arrange
    MockMultipartFile uploadedFile
      = new MockMultipartFile(
      "file",
      "hello.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".getBytes()
    );

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));
    doNothing().when(messageService).uploadMediaMessage(1L, uploadedFile, 1L);

    //Act & Assert
    mockMvc.perform(multipart("/messages/chat/1/media")
        .file(uploadedFile)
        .contentType(MediaType.MULTIPART_FORM_DATA)
     ).andExpect(status().isOk());
    //Verify
    verify(messageService, times(1)).uploadMediaMessage(
      argThat(chatId -> chatId == 1L),
      argThat(file -> file.equals(uploadedFile)),
        argThat(senderId -> senderId.equals(1L))
    );

  }

  @Test
  @WithMockUser(username= "alexandru.iordan99@gmail.com")
  void test_isChatRead() throws Exception{
    //Arrange
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));
    when(messageService.isChatRead(1L, 1L)).thenReturn(true);

    //Act & Assert
    mockMvc.perform(get("/messages/chat/1/is-read")
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    //Verify
    verify(messageService, times(1)).isChatRead(
      argThat(chatId -> chatId == 1L),
      argThat(senderId -> senderId.equals(1L)));

  }

  @Test
  @WithMockUser(username= "alexandru.iordan99@gmail.com")
  void test_getUnreadMessagesCount() throws Exception{
    //Arrange
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com")).thenReturn(Optional.ofNullable(mockUser));
    when(messageService.getUnreadCount(1L, 1L)).thenReturn(1);
    //Act & Assert
    mockMvc.perform(get("/messages/chat/1/unread-count")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
      {"unreadCount": 1}
  """)).andExpect(status().isOk());

    //Verify
    verify(messageService, times(1))
      .getUnreadCount(argThat(chatId -> chatId == 1L),
        argThat(senderId-> senderId.equals(1L)));

  }

}



