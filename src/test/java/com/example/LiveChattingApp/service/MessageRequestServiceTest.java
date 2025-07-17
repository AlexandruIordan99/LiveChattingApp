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

    directChat = Chat.builder()
      .id(1L)
      .type(ChatType.DIRECT)
      .creator(user1)
      .participants(Set.of(user1, user2))
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
  void getOrCreateMessageRequest_WhenExistingRequestExists_ShouldUpdateExistingRequest() {
    //Arrange
    String senderId = "1";
    String receiverId = "2";
    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .receiverId(receiverId)
      .status(MessageRequestStatus.PENDING)
      .type(MessageType.TEXT)
      .firstMessages(new ArrayList<>(List.of(
        Message.builder().content("Hello!").sender(user1).build(),
        Message.builder().content("How are you?").sender(user1).build()
      )))
      .build();

    when(messageRequestRepository.findBySenderIdAndReceiverIdAndMessageRequestStatus(
      senderId, receiverId, MessageRequestStatus.PENDING))
      .thenReturn(Optional.of(pendingRequest));
    when(messageRequestRepository.save(any(MessageRequest.class)))
      .thenReturn(pendingRequest);

    //Act
    MessageRequest result = messageRequestService.getOrCreateMessageRequest(newRequest, senderId, receiverId);


    //Assert
    assertNotNull(result);
    assertEquals(4, result.getFirstMessages().size());
    assertTrue(result.getFirstMessages().stream().anyMatch(msg -> msg.getContent().equals("Hello!")));
    verify(messageRequestRepository).save(pendingRequest);
    verify(messageRequestRepository).findBySenderIdAndReceiverIdAndMessageRequestStatus(
      senderId, receiverId, MessageRequestStatus.PENDING);
  }

  @Test
  void getOrCreateMessageRequest_WhenNoExistingRequest_ShouldCreateNewRequest() {
    //Act
    String senderId = "1";
    String receiverId = "2";
    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .receiverId(receiverId)
      .status(MessageRequestStatus.PENDING)
      .type(MessageType.TEXT)
      .firstMessages(new ArrayList<>(List.of(Message.builder().content("Hello!").sender(user1).build())))
      .build();

    when(messageRequestRepository.findBySenderIdAndReceiverIdAndMessageRequestStatus(
      senderId, receiverId, MessageRequestStatus.PENDING))
      .thenReturn(Optional.empty());

    ArgumentCaptor<MessageRequest> requestCaptor = ArgumentCaptor.forClass(MessageRequest.class);
    when(messageRequestRepository.save(requestCaptor.capture()))
      .thenReturn(newRequest);

    //Arrange
    MessageRequest result = messageRequestService.getOrCreateMessageRequest(newRequest, senderId, receiverId);


    //Assert
    assertNotNull(result);
    MessageRequest savedRequest = requestCaptor.getValue();
    assertEquals(senderId, savedRequest.getSenderId());
    assertEquals(receiverId, savedRequest.getReceiverId());
    assertEquals(MessageRequestStatus.PENDING, savedRequest.getStatus());
    assertEquals(MessageType.TEXT, savedRequest.getType());
    assertEquals(1, savedRequest.getFirstMessages().size());

    List<String> messageContents = savedRequest.getFirstMessages().stream()
      .map(Message::getContent)
      .toList();

    assertTrue(messageContents.contains("Hello!"));

    verify(messageRequestRepository).save(any(MessageRequest.class));
    verify(messageRequestRepository).findBySenderIdAndReceiverIdAndMessageRequestStatus(
      senderId, receiverId, MessageRequestStatus.PENDING);
  }

  @Test
  void extractMessageRequestContent_WhenRequestIsAccepted_ShouldCreateMessageAndDeleteRequest() {
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
  void extractMessageRequestContent_WhenRequestIsDeclined_ShouldDeleteRequest() {
    //Act
    Long chatId = 1L;

    //Arrange
    messageRequestService.declineMessageRequest(declinedRequest, chatId);

    // Assert
    verify(messageRequestRepository).delete(declinedRequest);
    verify(userRepository, never()).findById(anyString());
    verify(chatRepository, never()).findById(anyLong());
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  void extractMessageRequestContent_WhenRequestIsPending_ShouldThrowException() {
    //Act
    Long chatId = 1L;

    //Arrange & Assert
    assertThatThrownBy(()-> messageRequestService.extractMessageRequestContent(pendingRequest, chatId))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Can only process accepted requests.");

  }

  @Test
  void extractMessageRequestContent_WhenAcceptedButSenderNotFound_ShouldThrowException() {
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
  void extractMessageRequestContent_WhenAcceptedButChatNotFound_ShouldThrowException() {
    // Arrange
    Long chatId = 1L;
    when(userRepository.findById("1")).thenReturn(Optional.of(user1));
    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // Act & Assert
    EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
      messageRequestService.extractMessageRequestContent(acceptedRequest, chatId);
    });

    assertEquals("Chat not found.", exception.getMessage());
    verify(userRepository).findById("1");
    verify(chatRepository).findById(chatId);
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

  @Test
  void getOrCreateMessageRequest_WhenRequestHasEmptyFirstMessages_ShouldHandleCorrectly() {
    //Act
    String senderId = "1";
    String receiverId = "2";
    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .receiverId(receiverId)
      .status(MessageRequestStatus.PENDING)
      .type(MessageType.TEXT)
      .firstMessages(new ArrayList<>())
      .build();

    when(messageRequestRepository.findBySenderIdAndReceiverIdAndMessageRequestStatus(
      senderId, receiverId, MessageRequestStatus.PENDING))
      .thenReturn(Optional.empty());

    ArgumentCaptor<MessageRequest> requestCaptor = ArgumentCaptor.forClass(MessageRequest.class);
    when(messageRequestRepository.save(requestCaptor.capture()))
      .thenReturn(newRequest);

    //Arrange
    MessageRequest result = messageRequestService.getOrCreateMessageRequest(newRequest, senderId, receiverId);

    //Assert
    assertNotNull(result);
    MessageRequest savedRequest = requestCaptor.getValue();
    assertTrue(savedRequest.getFirstMessages().isEmpty());
    verify(messageRequestRepository).save(any(MessageRequest.class));
  }


}
