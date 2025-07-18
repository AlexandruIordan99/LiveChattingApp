package com.example.LiveChattingApp.service;


import com.example.LiveChattingApp.chat.*;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageRepository;
import com.example.LiveChattingApp.message.MessageType;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.messageRequest.MessageRequestRepository;
import com.example.LiveChattingApp.messageRequest.MessageRequestService;
import com.example.LiveChattingApp.messageRequest.MessageRequestStatus;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class MessageRequestServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private MessageRequestRepository messageRequestRepository;

  @InjectMocks
  private MessageRequestService messageRequestService;


  private User user1;
  private User user2;
  private User user3;
  private Chat directChat;
  private Chat groupChat;
  private final LocalDateTime testTime = LocalDateTime.now();
  private MessageRequest pendingRequest;
  private MessageRequest acceptedRequest;
  private MessageRequest declinedRequest;
  private MessageRequest messageRequest;

  @BeforeEach
  void setUp(){
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
      .id(1L)
      .type(ChatType.DIRECT)
      .creator(user1)
      .participants(Set.of(user1, user2, user3))
      .lastReadTimestamps(new HashMap<>())
      .messages(new ArrayList<>())
      .build();

    ArrayList<Message> messages = new ArrayList<>(List.of(
      Message.builder().content("Hello!").sender(user1).build(),
      Message.builder().content("How are you?").sender(user1).build()
    ));

    pendingRequest = MessageRequest.builder()
      .id(1L)
      .senderId("1")
      .receiverId("2")
      .status(MessageRequestStatus.PENDING)
      .firstMessages(messages)
      .build();

    acceptedRequest = MessageRequest.builder()
      .id(2L)
      .senderId("1")
      .receiverId("2")
      .status(MessageRequestStatus.ACCEPTED)
      .firstMessages(messages)
      .build();

    declinedRequest = MessageRequest.builder()
      .id(3L)
      .senderId("1")
      .receiverId("2")
      .status(MessageRequestStatus.DECLINED)
      .firstMessages(messages)
      .build();

  }

  @Test
  void test_createMessageRequest_WhenChatExists_ShouldCreateNewRequest() {
    // Arrange
    String senderId = "1";
    Long chatId = 1L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    ArgumentCaptor<MessageRequest> requestCaptor = ArgumentCaptor.forClass(MessageRequest.class);
    when(messageRequestRepository.save(requestCaptor.capture())).thenReturn(pendingRequest);

    // Act
    MessageRequest result = messageRequestService.createMessageRequest(senderId, chatId);

    // Assert
    assertNotNull(result);
    MessageRequest savedRequest = requestCaptor.getValue();
    assertEquals(senderId, savedRequest.getSenderId());
    assertEquals(directChat, savedRequest.getChat());
    assertEquals(MessageRequestStatus.PENDING, savedRequest.getStatus());

    verify(chatRepository).findById(chatId);
    verify(messageRequestRepository).save(any(MessageRequest.class));
  }

  @Test
  void test_createMessageRequest_WhenChatNotFound_ShouldThrowException() {
    // Arrange
    String senderId = "1";
    Long chatId = 1L;

    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> messageRequestService.createMessageRequest(senderId, chatId))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Chat not found.");

    verify(chatRepository).findById(chatId);
    verify(messageRequestRepository, never()).save(any(MessageRequest.class));
  }

  @Test
  void test_addToFirstMessages_WhenRequestExists_ShouldAddMessage() {
    // Arrange
    Long requestId = 1L;
    Message newMessage = Message.builder()
      .content("New message")
      .sender(user1)
      .type(MessageType.TEXT)
      .build();

    when(messageRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
    when(messageRequestRepository.save(any(MessageRequest.class))).thenReturn(pendingRequest);

    // Act
    messageRequestService.addToFirstMessages(requestId, newMessage);

    // Assert
    assertTrue(pendingRequest.getFirstMessages().contains(newMessage));
    assertEquals(3, pendingRequest.getFirstMessages().size());
    verify(messageRequestRepository).findById(requestId);
    verify(messageRequestRepository).save(pendingRequest);
  }

  @Test
  void test_addToFirstMessages_WhenRequestNotFound_ShouldThrowException() {
    // Arrange
    Long requestId = 1L;
    Message newMessage = Message.builder()
      .content("New message")
      .sender(user1)
      .type(MessageType.TEXT)
      .build();

    when(messageRequestRepository.findById(requestId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> messageRequestService.addToFirstMessages(requestId, newMessage))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Message request not found.");

    verify(messageRequestRepository).findById(requestId);
    verify(messageRequestRepository, never()).save(any(MessageRequest.class));
  }

  @Test
  void test_addToFirstMessages_WhenMaximumMessagesReached_ShouldThrowException() {
    // Arrange
    Long requestId = 1L;
    Message newMessage = Message.builder()
      .content("New message")
      .sender(user1)
      .type(MessageType.TEXT)
      .build();

    // Create a request with 15 messages (maximum)
    ArrayList<Message> maxMessages = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      maxMessages.add(Message.builder()
        .content("Message " + i)
        .sender(user1)
        .type(MessageType.TEXT)
        .build());
    }

    MessageRequest fullRequest = MessageRequest.builder()
      .id(requestId)
      .senderId("1")
      .chat(directChat)
      .status(MessageRequestStatus.PENDING)
      .firstMessages(maxMessages)
      .build();

    when(messageRequestRepository.findById(requestId)).thenReturn(Optional.of(fullRequest));

    // Act & Assert
    assertThatThrownBy(() -> messageRequestService.addToFirstMessages(requestId, newMessage))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("You've reached the maximum number of messages before the user accepts your request.");

    verify(messageRequestRepository).findById(requestId);
    verify(messageRequestRepository, never()).save(any(MessageRequest.class));
  }

  @Test
  void test_findExistingRequest_WhenRequestExists_ShouldReturnRequest() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";

    when(messageRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId))
      .thenReturn(Optional.of(pendingRequest));

    // Act
    Optional<MessageRequest> result = messageRequestService.findExistingRequest(senderId, receiverId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(pendingRequest, result.get());
    verify(messageRequestRepository).findBySenderIdAndReceiverId(senderId, receiverId);
  }

  @Test
  void test_findExistingRequest_WhenRequestNotExists_ShouldReturnEmpty() {
    // Arrange
    String senderId = "1";
    String receiverId = "2";

    when(messageRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId))
      .thenReturn(Optional.empty());

    // Act
    Optional<MessageRequest> result = messageRequestService.findExistingRequest(senderId, receiverId);

    // Assert
    assertFalse(result.isPresent());
    verify(messageRequestRepository).findBySenderIdAndReceiverId(senderId, receiverId);
  }

  @Test
  void test_extractMessageRequestContent_WhenChatNotFound_ShouldThrowException() {
    //Arrange
    Long chatId = 1L;
    when(userRepository.findById("1")).thenReturn(Optional.of(user1));
    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
    //Act & Assert

    EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
      () -> messageRequestService.extractMessageRequestContent(acceptedRequest, chatId));

    assertEquals("Chat not found.", exception.getMessage());
    verify(userRepository).findById("1");
    verify(chatRepository).findById(chatId);
    verify(messageRepository, never()).save(any(Message.class));
    verify(messageRequestRepository, never()).delete(any(MessageRequest.class));
  }

  @Test
  void test_extractMessageRequestContent_WhenRequestIsAccepted_ShouldCreateMessageAndDeleteRequest() {
    // Act
    Long chatId = 1L;
    when(userRepository.findById("1")).thenReturn(Optional.of(user1));
    when(chatRepository.findById(chatId)).thenReturn(Optional.of(directChat));

    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    when(messageRepository.save(messageCaptor.capture())).thenReturn(new Message());

    // Arrange
    messageRequestService.extractMessageRequestContent(acceptedRequest, chatId);

    //Assert
    Message savedMessage = messageCaptor.getValue();
    assertNotNull(savedMessage);
    assertEquals(user1, savedMessage.getSender());
    assertEquals(directChat, savedMessage.getChat());
    assertEquals(acceptedRequest.getFirstMessages().toString(), savedMessage.getContent());

    verify(userRepository).findById("1");
    verify(chatRepository).findById(chatId);
    verify(messageRepository, times(2)).save(any(Message.class));
    verify(messageRequestRepository).delete(acceptedRequest);
  }

  @Test
  void test_extractMessageRequestContent_WhenRequestIsPending_ShouldThrowException() {
    //Act
    Long chatId = 1L;

    //Arrange & Assert
    assertThatThrownBy(()-> messageRequestService.extractMessageRequestContent(pendingRequest, chatId))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Can only process accepted requests.");

  }

  @Test
  void test_extractMessageRequestContent_WhenAcceptedButSenderNotFound_ShouldThrowException() {
    //Arrange
    Long chatId = 1L;
    when(userRepository.findById("1")).thenReturn(Optional.empty());

    //Act & Assert
    EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
      messageRequestService.extractMessageRequestContent(acceptedRequest, chatId);
    });

    assertEquals("Sender not found.", exception.getMessage());
    verify(userRepository).findById("1");
    verify(chatRepository, never()).findById(anyLong());
    verify(messageRepository, never()).save(any(Message.class));
    verify(messageRequestRepository, never()).delete(any(MessageRequest.class));
  }

  @Test
  void test_extractMessageRequestContent() {
    //Arrange
    ArrayList<Message> messages = new ArrayList<>(List.of(
      Message.builder().content("Hello!").sender(user1).build(),
      Message.builder().content("How are you?").sender(user1).build()
    ));

    String senderId = "1";
    String receiverId = "2";
    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .receiverId(receiverId)
      .status(MessageRequestStatus.PENDING)
      .type(MessageType.TEXT)
      .firstMessages(messages)
      .build();

    //Act & Assert
    assertTrue(newRequest.getFirstMessages().containsAll(messages));
  }


}
