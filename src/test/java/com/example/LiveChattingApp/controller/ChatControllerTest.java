package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.chat.ChatType;
import com.example.LiveChattingApp.message.MessageService;
import com.example.LiveChattingApp.security.UserSynchronizer;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserDTO;
import com.example.LiveChattingApp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.mockito.Mockito.*;
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
  private User mockUser1;
  private User mockUser2;
  private User mockUser3;
  private Set<String> chatParticipantsIds;
  private MockMvc mockMvc;

  @Autowired
  private MessageService messageService;

  @MockitoBean
  private UserDTO userDTO;
  private UserDTO mockUser2DTO;
  private UserDTO mockUser3DTO;

  private Chat chatToBeFound;

  @MockitoBean
  private UserSynchronizer userSynchronizer;
  @MockitoBean
  private UserRepository userRepository;

  @BeforeEach
  void setUp(){
    mockMvc = MockMvcBuilders
      .webAppContextSetup(context)
      .apply(springSecurity())
      .build();

    objectMapper = new ObjectMapper();

    mockUser1 = User.builder()
      .id("1")
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();

    mockUser2 = User.builder()
      .id("2")
      .displayName("gtgmycatisonfire")
      .email("catonfire99@gmail.com")
      .build();

     mockUser2DTO = UserDTO.builder()
      .id("2")
      .email("catonfire99@gmail.com")
      .displayName("gtgmycatisonfire")
      .build();

    mockUser3 = User.builder()
      .id("3")
      .displayName("copilcoiot")
      .email("coiot2000@gmail.com")
      .build();

    mockUser3DTO = UserDTO.builder()
      .id("3")
      .email("coiot2000@gmail.com")
      .displayName("copilcoiot")
      .build();

    chatParticipantsIds = new HashSet<String>();
    chatParticipantsIds.add(mockUser2.getId());
    chatParticipantsIds.add(mockUser3.getId());

    chatToBeFound = Chat.builder()
      .id(1L)
      .type(ChatType.DIRECT)
      .build();

  }


  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_createDirectChat() throws Exception {
    //Arrange

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));
    when(chatService.createDirectChat("1", mockUser2DTO.getId()))
      .thenReturn(1L);

    String requestBody = objectMapper.writeValueAsString(mockUser2DTO);

    //Act & Assert
    mockMvc.perform(post("/chats/direct")
      .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isOk());

    //Verify
    verify(chatService, times(1)).createDirectChat(
      argThat(senderId -> senderId.equals("1")),
        argThat(receiverId -> receiverId.equals("2")));
  }

  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_createGroupChat() throws Exception{
    //Arrange
    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));
    when(chatService.createGroupChat("1","Nu joc helldivers", chatParticipantsIds))
      .thenReturn(1L);

  String requestBody = objectMapper.writeValueAsString(Map.of(
    "name", "Nu joc helldivers", "participantIds", chatParticipantsIds
  ));

    //Act & Assert
    mockMvc.perform(post("/chats/group")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isOk());

    //Verify
  verify(chatService, times(1)).createGroupChat(
    argThat(creatorId -> creatorId.equals("1")),
      argThat(chatName-> chatName.equals("Nu joc helldivers")),
      argThat(partIds-> partIds.equals(chatParticipantsIds)));

  }

  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_addParticipant() throws Exception{
    //Arrange
    Long chatId = 1L;
    String userIdToAdd = "3";

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));
    doNothing().when(chatService).addParticipantToGroup(chatId, userIdToAdd, "1");
    //Act & Assert
    mockMvc.perform(post("/chats/{chatId}/participants", chatId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(userIdToAdd)) // JSON string format for String parameter
      .andExpect(status().isOk());

    //Verify
    verify(chatService, times(1))
      .addParticipantToGroup(chatId, userIdToAdd, "1");

  }
  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_removeParticipant() throws Exception{
    //Arrange
    Long chatId = 1L;
    String userIdToRemove = "2";

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));
    doNothing().when(chatService).removeParticipantFromGroup(chatId, userIdToRemove, "1");

    //Act & Assert
    mockMvc.perform(delete("/chats/{chatId}/participants/{userId}", chatId, userIdToRemove))
      .andExpect(status().isOk());

    //Verify
    verify(chatService, times(1)).removeParticipantFromGroup(chatId, userIdToRemove, "1");

  }

  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_getChatParticipants() throws Exception{
    //Arrange
    Long chatId = 1L;
    Set<User> participants = Set.of(mockUser1, mockUser2);

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));

    when(chatService.isUserParticipant(chatId, "1")).thenReturn(true);
    when(chatService.getChatParticipants(chatId)).thenReturn(participants);

    //Act & Assert
    mockMvc.perform(get("/chats/1/participants"))
      .andExpect(status().isOk());

    //Verify
    verify(chatService, times(1))
      .isUserParticipant(chatId, "1");
    verify(chatService, times(1))
      .getChatParticipants(chatId);

  }

  @Test
  @WithMockUser(username = "alexandru.iordan99@gmail.com")
  void test_leaveChat() throws Exception{
    //Arrange
    Long chatId = 1L;

    when(userRepository.findByEmail("alexandru.iordan99@gmail.com"))
      .thenReturn(Optional.ofNullable(mockUser1));
    doNothing().when(chatService).removeParticipantFromGroup(chatId, "1", "1");

    //Act & Assert
    mockMvc.perform(post("/chats/{chatId}/leave", chatId))
      .andExpect(status().isOk());

    //Verify
    verify(chatService, times(1)).removeParticipantFromGroup(chatId, "1", "1");

  }


}
