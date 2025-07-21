package com.example.LiveChattingApp.service;


import com.example.LiveChattingApp.chat.*;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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


  @Test
  void resolveReceiverIdFromChat_ReturnsReceiverId() {
    // Arrange
    String senderId = "1";

    // Act
    String receiverId = chatService.resolveReceiverIdFromChat(directChat, senderId);

    // Assert
    assertEquals(senderId, receiverId);
  }

  @Test
  void resolveReceiverIdFromChat_ThrowsException_WhenReceiverNotFound() {
    // Arrange
    String senderId = "999";

    // Act & Assert
    assertThrows(IllegalStateException.class, () ->
      chatService.resolveReceiverIdFromChat(directChat, senderId));
  }

  @Test
  void createDirectChat_ReturnExistingChatId_WhenChatExists() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";

    when(chatRepository.findDirectChatBetweenUsers(senderId, receiverId))
      .thenReturn(Optional.of(directChat));

    // Act
    Long result = chatService.createDirectChat(senderId, receiverId);

    // Assert
    assertEquals(1L, result);
    verify(chatRepository).findDirectChatBetweenUsers(senderId, receiverId);
    verify(chatRepository, never()).save(any());
  }

  @Test
  void createDirectChat_CreateNewChat_WhenChatDoesNotExist() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Chat savedChat = Chat.builder().id(3L).build();

    when(chatRepository.findDirectChatBetweenUsers(senderId, receiverId))
      .thenReturn(Optional.empty());
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(userRepository.findById(receiverId)).thenReturn(Optional.of(user2));
    when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

    // Act
    Long result = chatService.createDirectChat(senderId, receiverId);

    // Assert
    assertEquals(3L, result);
    verify(chatRepository).save(any(Chat.class));
  }

  @Test
  void createDirectChat_ThrowException_WhenSenderNotFound() {
    // Arrange
    String senderId = "999";
    String receiverId = "2";

    when(chatRepository.findDirectChatBetweenUsers(senderId, receiverId))
      .thenReturn(Optional.empty());
    when(userRepository.findById(senderId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.createDirectChat(senderId, receiverId));
  }

  @Test
  void createDirectChat_ThrowsException_WhenReceiverNotFound() {
    // Arrange
    String senderId = "1";
    String receiverId = "999";

    when(chatRepository.findDirectChatBetweenUsers(senderId, receiverId))
      .thenReturn(Optional.empty());
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.createDirectChat(senderId, receiverId));
  }

  @Test
  void createGroupChat_ShouldCreateNewGroupChat() {
    // Arrange
    String creatorId = "1";
    String chatName = "Test Group";
    Set<String> participantIds = Set.of("2", "3");
    Chat savedChat = Chat.builder().id(4L).build();

    when(userRepository.findById(creatorId)).thenReturn(Optional.of(user1));
    when(userRepository.findById("2")).thenReturn(Optional.of(user2));
    when(userRepository.findById("3")).thenReturn(Optional.of(user3));
    when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

    // Act
    Long result = chatService.createGroupChat(creatorId, chatName, participantIds);

    // Assert
    assertEquals(4L, result);
    verify(chatRepository).save(any(Chat.class));
  }

  @Test
  void createGroupChat_ShouldThrowException_WhenCreatorNotFound() {
    // Arrange
    String creatorId = "999";
    String chatName = "Test Group";
    Set<String> participantIds = Set.of("2", "3");

    when(userRepository.findById(creatorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.createGroupChat(creatorId, chatName, participantIds));
  }

  @Test
  void createGroupChat_ShouldThrowException_WhenParticipantNotFound() {
    // Arrange
    String creatorId = "1";
    String chatName = "Test Group";
    Set<String> participantIds = Set.of("2", "999");

    when(userRepository.findById(creatorId)).thenReturn(Optional.of(user1));
    when(userRepository.findById("999")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.createGroupChat(creatorId, chatName, participantIds));

    verify(userRepository).findById(creatorId);
  }

  @Test
  void addParticipantToGroup_ShouldAddParticipant_WhenValidRequest() {
    // Arrange
    Long chatId = 2L;
    String userId = "4";
    String addedByUserId = "1";
    User newUser = User.builder().build();
    newUser.setId("4");

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
    when(userRepository.findById(userId)).thenReturn(Optional.of(newUser));

    // Act
    chatService.addParticipantToGroup(chatId, userId, addedByUserId);

    // Assert
    verify(chatRepository).save(groupChat);
    assertTrue(groupChat.getParticipants().contains(newUser));
  }


  @Test
  void addParticipantToGroup_ShouldThrowException_WhenChatNotFound() {
    // Arrange
    Long chatId = 999L;
    String userId = "4";
    String addedByUserId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.addParticipantToGroup(chatId, userId, addedByUserId));
  }

  @Test
  void addParticipantToGroup_ShouldThrowException_WhenNotGroupChat() {
    // Arrange
    Long chatId = 1L;
    String userId = "4";
    String addedByUserId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      chatService.addParticipantToGroup(chatId, userId, addedByUserId));
  }

  @Test
  void addParticipantToGroup_ShouldThrowException_WhenNotAdmin() {
    // Arrange
    Long chatId = 2L;
    String userId = "4";
    String addedByUserId = "2"; // user2 is not admin

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      chatService.addParticipantToGroup(chatId, userId, addedByUserId));
  }

  @Test
  void removeParticipantFromGroup_ShouldRemoveParticipant_WhenAdminRemovesOther() {
    // Arrange
    Long chatId = 2L;
    String userId = "3";
    String removedByUserId = "1"; // admin

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user3));

    // Act
    chatService.removeParticipantFromGroup(chatId, userId, removedByUserId);

    // Assert
    verify(chatRepository).save(groupChat);
    assertFalse(groupChat.getParticipants().contains(user3));
  }

  @Test
  void removeParticipantFromGroup_ShouldRemoveParticipant_WhenUserRemovesThemself() {
    // Arrange
    Long chatId = 2L;
    String userId = "3";
    String removedByUserId = "3";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user3));

    // Act
    chatService.removeParticipantFromGroup(chatId, userId, removedByUserId);

    // Assert
    verify(chatRepository).save(groupChat);
    assertFalse(groupChat.getParticipants().contains(user3));
  }

  @Test
  void removeParticipantFromGroup_ShouldThrowException_WhenNonAdminRemovesOther() {
    // Arrange
    Long chatId = 2L;
    String userId = "3";
    String removedByUserId = "2";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      chatService.removeParticipantFromGroup(chatId, userId, removedByUserId));
  }

  @Test
  void getChatParticipants_ShouldReturnParticipants() {
    // Arrange
    Long chatId = 2L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    Set<User> participants = chatService.getChatParticipants(chatId);

    // Assert
    assertEquals(3, participants.size());
    assertTrue(participants.contains(user1));
    assertTrue(participants.contains(user2));
    assertTrue(participants.contains(user3));
  }


  @Test
  void isUserParticipant_ShouldReturnTrue_WhenUserIsParticipant() {
    // Arrange
    Long chatId = 2L;
    String userId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    boolean result = chatService.isUserParticipant(chatId, userId);

    // Assert
    assertTrue(result);
  }

  @Test
  void isUserParticipant_ShouldReturnFalse_WhenUserIsNotParticipant() {
    // Arrange
    Long chatId = 2L;
    String userId = "999";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    boolean result = chatService.isUserParticipant(chatId, userId);

    // Assert
    assertFalse(result);
  }

  @Test
  void makeUserAdmin_ShouldAddUserToAdmins() {
    // Arrange
    Long chatId = 2L;
    String userId = "2";
    String requestedByUserId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    chatService.makeUserAdmin(chatId, userId, requestedByUserId);

    // Assert
    verify(chatRepository).save(groupChat);
    assertTrue(groupChat.getAdminUserIds().contains(userId));
  }

  @Test
  void makeUserAdmin_ShouldThrowException_WhenRequesterNotAdmin() {
    // Arrange
    Long chatId = 2L;
    String userId = "2";
    String requestedByUserId = "3";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      chatService.makeUserAdmin(chatId, userId, requestedByUserId));
  }

  @Test
  void removeAdminRole_ShouldRemoveUserFromAdmins() {
    // Arrange
    Long chatId = 2L;
    String userId = "2";
    String requestedByUserId = "1";

    Chat testChat = new Chat();
    testChat.setId(chatId);
    testChat.setAdminUserIds(new HashSet<>(Arrays.asList("2")));

    User creator = new User();
    creator.setId("1");
    testChat.setCreator(creator);

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(testChat));

    // Act
    chatService.removeAdminRole(chatId, userId, requestedByUserId);

    // Assert
    verify(chatRepository).save(testChat);
    assertFalse(testChat.getAdminUserIds().contains(userId));
  }

  @Test
  void removeAdminRole_ShouldThrowException_WhenTryingToRemoveCreator() {
    // Arrange
    Long chatId = 2L;
    String userId = "1";
    String requestedByUserId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      chatService.removeAdminRole(chatId, userId, requestedByUserId));
  }

  @Test
  void markAsRead_ShouldUpdateLastReadTimestamp() {
    // Arrange
    Long chatId = 2L;
    String userId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    chatService.markAsRead(chatId, userId);

    // Assert
    verify(chatRepository).save(groupChat);
    assertNotNull(groupChat.getLastReadTimestamps().get(userId));
  }

  @Test
  void getUnreadCount_ShouldReturnZero_WhenNoMessages() {
    // Arrange
    Long chatId = 2L;
    String userId = "1";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    long unreadCount = chatService.getUnreadCount(chatId, userId);

    // Assert
    assertEquals(0, unreadCount);
  }

  @Test
  void findChatById_ShouldReturnChat_WhenChatExists() {
    // Arrange
    Long chatId = 1L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    Chat result = chatService.findChatById(chatId);

    // Assert
    assertEquals(directChat, result);
  }

  @Test
  void findChatById_ShouldThrowException_WhenChatNotFound() {
    // Arrange
    Long chatId = 999L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      chatService.findChatById(chatId));
}

}
