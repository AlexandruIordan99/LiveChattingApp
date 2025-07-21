package com.example.LiveChattingApp.service;


import com.example.LiveChattingApp.chat.*;
import com.example.LiveChattingApp.file.FileService;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.message.*;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.messageRequest.MessageRequestService;
import com.example.LiveChattingApp.messageRequest.MessageRequestStatus;
import com.example.LiveChattingApp.notification.Notification;
import com.example.LiveChattingApp.notification.NotificationService;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class MessageServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MessageMapper mapper;

  @Mock
  private Authentication authentication;

  @Mock
  private FriendshipService friendshipService;

  @Mock
  private MessageRequestService messageRequestService;

  @Mock
  private FileService fileService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private MultipartFile multipartFile;

  @Mock
  Notification notification;

  @Mock
  private ChatService chatService;

  @InjectMocks
  private MessageService messageService;

  private User user1;
  private User user2;
  private User user3;
  private Chat directChat;
  private Chat groupChat;

  private final LocalDateTime testTime = LocalDateTime.now();
  private MessageRequest messageRequest;
  private Message message;


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

    message = Message.builder()
      .id(1L)
      .sender(user1)
      .chat(directChat)
      .content("Hello")
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .createdDate(testTime)
      .build();

    messageRequest = MessageRequest.builder()
      .id(1L)
      .senderId("1")
      .chat(directChat)
      .status(MessageRequestStatus.PENDING)
      .firstMessages(new ArrayList<>())
      .build();


  }

  @Test
  void test_sendDirectMessage_whenUsersAreFriends_savesMessage() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 1L;
    String content = "Hello there!";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(friendshipService.existsFriendshipBetweenUsers(senderId, receiverId)).thenReturn(true);
    when(messageRepository.save(any(Message.class))).thenReturn(message);

    // Act
    messageService.sendDirectMessage(senderId, receiverId, chatId, content);

    // Assert
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).save(messageCaptor.capture());

    Message savedMessage = messageCaptor.getValue();
    assertEquals(user1, savedMessage.getSender());
    assertEquals(directChat, savedMessage.getChat());
    assertEquals(content, savedMessage.getContent());
    assertEquals(MessageType.TEXT, savedMessage.getType());
    assertEquals(MessageState.SENT, savedMessage.getState());

    verify(messageRequestService, never()).createMessageRequest(anyString(), anyLong());
  }


  @Test
  void test_sendDirectMessage_whenUsersAreNotFriends_createsMessageRequest() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 1L;
    String content = "Hello there!";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(friendshipService.existsFriendshipBetweenUsers(senderId, receiverId)).thenReturn(false);
    when(messageRequestService.findExistingRequest(senderId, receiverId)).thenReturn(Optional.empty());
    when(messageRequestService.createMessageRequest(senderId, chatId)).thenReturn(messageRequest);
    when(messageRepository.save(any(Message.class))).thenReturn(message);

    // Act
    messageService.sendDirectMessage(senderId, receiverId, chatId, content);

    // Assert
    verify(messageRequestService).createMessageRequest(senderId, chatId);
    verify(messageRequestService).addToFirstMessages(eq(messageRequest.getId()), any(Message.class));
    verify(messageRepository).save(any(Message.class));
  }


  @Test
  void test_sendDirectMessage_withEmptyContent_throwsIllegalArgumentException() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 1L;
    String content = "";

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      messageService.sendDirectMessage(senderId, receiverId, chatId, content));
  }

  @Test
  void test_sendDirectMessage_withNullContent_throwsIllegalArgumentException() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 1L;
    String content = null;

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      messageService.sendDirectMessage(senderId, receiverId, chatId, content));
  }

  @Test
  void test_sendDirectMessage_withNonExistentChat_throwsEntityNotFoundException() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 999L;
    String content = "Hello";

    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () ->
      messageService.sendDirectMessage(senderId, receiverId, chatId, content));
  }

  @Test
  void test_sendDirectMessage_withGroupChat_throwsRuntimeException() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";
    Long chatId = 2L;
    String content = "Hello";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.sendDirectMessage(senderId, receiverId, chatId, content));
  }


  @Test
  void test_sendGroupMessage_whenSenderIsCreatorAndParticipantsAreFriends_savesMessage() {
    // Arrange
    String senderId = "1";
    Long chatId = 2L;
    String content = "Hello group!";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(friendshipService.existsFriendshipBetweenUsers(senderId, "2")).thenReturn(true);
    when(friendshipService.existsFriendshipBetweenUsers(senderId, "3")).thenReturn(true);
    when(messageRepository.save(any(Message.class))).thenReturn(message);

    // Act
    messageService.sendGroupMessage(senderId, chatId, content);

    // Assert
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository, atLeastOnce()).save(messageCaptor.capture());

    Message savedMessage = messageCaptor.getValue();
    assertEquals(user1, savedMessage.getSender());
    assertEquals(groupChat, savedMessage.getChat());
    assertEquals(content, savedMessage.getContent());
    assertEquals(MessageType.TEXT, savedMessage.getType());
    assertEquals(MessageState.SENT, savedMessage.getState());
  }

  @Test
  void test_sendGroupMessage_withEmptyContent_throwsIllegalArgumentException() {
    // Arrange
    String senderId = "1";
    Long chatId = 2L;
    String content = "";

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
      messageService.sendGroupMessage(senderId, chatId, content));
  }

  @Test
  void test_sendGroupMessage_withDirectChat_throwsRuntimeException() {
    // Arrange
    String senderId = "1";
    Long chatId = 1L;
    String content = "Hello";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.sendGroupMessage(senderId, chatId, content));
  }

  @Test
  void test_sendGroupMessage_whenSenderIsNotParticipant_throwsRuntimeException() {
    // Arrange
    String senderId = "999";
    Long chatId = 2L;
    String content = "Hello";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(User.builder().build()));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.sendGroupMessage(senderId, chatId, content));
  }

  @Test
  void test_getMessageReceiverId_withDirectChat_returnsReceiverId() {
    // Arrange
    String senderId = "1";
    Long chatId = 1L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    String receiverId = messageService.getMessageReceiverId(senderId, chatId);

    // Assert
    assertEquals("2", receiverId);
  }

  @Test
  void test_getMessageReceiverId_withGroupChat_throwsRuntimeException() {
    // Arrange
    String senderId = "1";
    Long chatId = 2L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.getMessageReceiverId(senderId, chatId));
  }

  @Test
  void test_getGroupChatParticipantsExceptSender_returnsCorrectParticipants() {
    // Arrange
    String senderId = "1";
    Long chatId = 2L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(groupChat));

    // Act
    Set<User> participants = messageService.getGroupChatParticipantsExceptSender(chatId, senderId);

    // Assert
    assertEquals(2, participants.size());
    assertTrue(participants.contains(user2));
    assertTrue(participants.contains(user3));
    assertFalse(participants.contains(user1));
  }

  @Test
  void test_getGroupChatParticipantsExceptSender_withDirectChat_throwsRuntimeException() {
    // Arrange
    String senderId = "1";
    Long chatId = 1L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.getGroupChatParticipantsExceptSender(chatId, senderId));
  }

  @Test
  void test_findChatMessages_whenUserIsParticipant_returnsMessages() {
    // Arrange
    Long chatId = 1L;
    String userId = "1";
    List<Message> messages = Arrays.asList(message);
    MessageResponse messageResponse = new MessageResponse();

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
    when(messageRepository.findMessagesByChatId(chatId)).thenReturn(messages);
    when(mapper.toMessageResponse(any(Message.class), eq(userId))).thenReturn(messageResponse);

    // Act
    List<MessageResponse> result = messageService.findChatMessages(chatId,"1");

    // Assert
    assertEquals(1, result.size());
    verify(messageRepository).findMessagesByChatId(chatId);
    verify(mapper).toMessageResponse(message, userId);
  }

  @Test
  void test_findChatMessages_whenUserIsNotParticipant_throwsRuntimeException() {
    // Arrange
    Long chatId = 1L;
    String userId = "999";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.findChatMessages(chatId, "1"));
  }

  @Test
  void test_uploadMediaMessage_savesMessageAndSendsNotifications() {
    // Arrange
    Long chatId = 1L;
    String senderId = "1";
    String filePath = "/path/to/file.jpg";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user1));
    when(fileService.saveFile(multipartFile, senderId)).thenReturn(filePath);
    when(messageRepository.save(any(Message.class))).thenReturn(message);

    // Act
    messageService.uploadMediaMessage(chatId, multipartFile, senderId);

    // Assert
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).save(messageCaptor.capture());

    Message savedMessage = messageCaptor.getValue();
    assertEquals(user1, savedMessage.getSender());
    assertEquals(directChat, savedMessage.getChat());
    assertEquals(MessageType.IMAGE, savedMessage.getType());
    assertEquals(filePath, savedMessage.getMediaFilePath());

    verify(notificationService).sendNotification(eq("2"), any(Notification.class));
  }

  @Test
  void test_markAsRead_updatesTimestamp() {
    // Arrange
    Long chatId = 1L;
    String userId = "1";
    Map<String, LocalDateTime> timestamps = new HashMap<>();
    directChat.setLastReadTimestamps(timestamps);

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));
    when(chatRepository.save(directChat)).thenReturn(directChat);

    // Act
    messageService.markAsRead(chatId, userId);

    // Assert
    assertNotNull(directChat.getLastReadTimestamps().get(userId));
    verify(chatRepository).save(directChat);
  }

  @Test
  void test_getUnreadCount_withNoLastReadTimestamp_returnsAllMessages() {
    // Arrange
    Long chatId = 1L;
    String userId = "1";
    List<Message> messages = Arrays.asList(message, message);
    directChat.setMessages(messages);

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    long unreadCount = messageService.getUnreadCount(chatId, userId);

    // Assert
    assertEquals(2, unreadCount);
  }

  @Test
  void test_getUnreadCount_withLastReadTimestamp_returnsCorrectCount() {
    // Arrange
    Long chatId = 1L;
    String userId = "2";
    LocalDateTime lastRead = testTime.minusHours(1);

    Message newMessage = Message.builder()
      .sender(user1)
      .createdDate(testTime)
      .build();

    Message oldMessage = Message.builder()
      .sender(user1)
      .createdDate(testTime.minusHours(2))
      .build();

    directChat.setMessages(Arrays.asList(newMessage, oldMessage));
    directChat.getLastReadTimestamps().put(userId, lastRead);

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    long unreadCount = messageService.getUnreadCount(chatId, userId);

    // Assert
    assertEquals(1, unreadCount);
  }

  @Test
  void test_isChatRead_withNoMessages_returnsTrue() {
    // Arrange
    Long chatId = 1L;
    String userId = "1";
    directChat.setMessages(new ArrayList<>());

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    boolean isRead = messageService.isChatRead(chatId, userId);

    // Assert
    assertTrue(isRead);
  }

  @Test
  void test_isChatRead_withUnreadMessages_returnsFalse() {
    // Arrange
    Long chatId = 1L;
    String userId = "1";
    LocalDateTime lastRead = testTime.minusHours(1);

    user2.setId("2");
    assertEquals("2", user2.getId());

    Message newMessage = Message.builder()
      .sender(user2)
      .chat(directChat)
      .createdDate(testTime)
      .build();

    directChat.setMessages(List.of(newMessage));
    directChat.setLastReadTimestamps(Map.of(userId, lastRead));

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act
    boolean isRead = messageService.isChatRead(chatId, userId);

    // Assert
    assertFalse(isRead);
  }

  @Test
  void test_isChatRead_whenUserIsNotParticipant_throwsRuntimeException() {
    // Arrange
    Long chatId = 1L;
    String userId = "999";

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      messageService.isChatRead(chatId, userId));
  }


}