package com.example.LiveChattingApp.service;


import com.example.LiveChattingApp.chat.*;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class ChatServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChatMapper mapper;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private ChatService chatService;


  private User user1;
  private User user2;
  private User user3;
  private Chat directChat;
  private Chat groupChat;
  private final LocalDateTime testTime = LocalDateTime.now();


  @BeforeEach
  void setUp() {
    user1 = User.builder()
      .firstname("Alexandru")
      .lastname("Iordan")
      .displayName("Jordan299")
      .password("alunemari1234")
      .dateOfBirth("30.11.1999")
      .email("alexandru.iordan99@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(testTime)
      .build();

    user1.setId("1");

    user2 = User.builder()
      .firstname("Vlad")
      .lastname("Loghin")
      .displayName("gtgmycatisonfire")
      .password("doomguy2000")
      .dateOfBirth("23.02.1996")
      .email("vladloghin00@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(testTime)
      .build();
    user2.setId("2");

    user3 = User.builder()
      .firstname("Matei")
      .lastname("Paulet")
      .displayName("copilcoiot")
      .password("coiot9000")
      .dateOfBirth("17.07.1995")
      .email("mateipaulet1999@gmail.com")
      .accountLocked(false)
      .enabled(true)
      .createdDate(testTime)
      .build();
    user3.setId("3");

    directChat = Chat.builder()
      .id(1L)
      .type(ChatType.DIRECT)
      .creator(user1)
      .participants(Set.of(user1, user2))
      .lastReadTimestamps(new HashMap<>())
      .messages(new ArrayList<>())
      .build();

    groupChat = Chat.builder()
      .id(2L)
      .name("Test Group")
      .type(ChatType.GROUP)
      .creator(user1)
      .participants(Set.of(user1, user2, user3))
      .adminUserIds(Set.of("1"))
      .lastReadTimestamps(new HashMap<>())
      .messages(new ArrayList<>())
      .build();

  }

  @Test
  void getChatsByReceiverId_ShouldReturnMappedChats() {
    //Arrange
    String userId = "1";
    List<Chat> chats = Arrays.asList(directChat, groupChat);
    ChatResponse response1 = ChatResponse.builder()
      .id(1L)
      .name("Direct Chat")
      .unreadCount(2)
      .lastMessage("Hello")
      .lastMessageTime(testTime)
      .isRecipientOnline(true)
      .senderId("1")
      .receiverId("2")
      .build();
    ChatResponse response2 = ChatResponse.builder()
      .id(2L)
      .name("Test Group")
      .unreadCount(0)
      .lastMessage("Welcome")
      .lastMessageTime(testTime)
      .isRecipientOnline(false)
      .senderId("1")
      .receiverId(null)
      .build();

    when(authentication.getName()).thenReturn(userId);
    when(chatRepository.findChatsByUserId(userId)).thenReturn(chats);
    when(mapper.toChatResponse(directChat, userId)).thenReturn(response1);
    when(mapper.toChatResponse(groupChat, userId)).thenReturn(response2);

    // Act
    List<ChatResponse> result = chatService.getChatsByReceiverId(authentication);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.contains(response1));
    assertTrue(result.contains(response2));
    verify(chatRepository).findChatsByUserId(userId);
    verify(mapper).toChatResponse(directChat, userId);
    verify(mapper).toChatResponse(groupChat, userId);
  }


}
