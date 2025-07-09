package com.example.LiveChattingApp.message;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.file.FileService;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final MessageMapper mapper;
  private final FileService fileService;

  public void saveMessage(MessageRequest messageRequest){
    Chat chat = chatRepository.findById(messageRequest.getChatId())
      .orElseThrow(() -> new RuntimeException("Chat not found"));

    User sender = userRepository.findById(messageRequest.getSenderId())
      .orElseThrow(() -> new RuntimeException("Sender not found"));

    User receiver = userRepository.findById(messageRequest.getReceiverId())
      .orElseThrow(() -> new RuntimeException("Receiver not found"));

    Message message = Message.builder()
      .content(messageRequest.getContent())
      .chat(chat)
      .sender(sender)
      .receiver(receiver)
      .type(messageRequest.getType())
      .state(MessageState.SENT)
      .build();

    messageRepository.save(message);

    //to do notifs!

  }

  public List<MessageResponse> findChatMessages(String chatId){
    return messageRepository.findMessageByChatId(chatId)
      .stream()
      .map(mapper::toMessageResponse)
      .toList();

  }

  public void setMessagesToSeen(String chatId, Authentication authentication){
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException(("Chat not found")));

    final String recipientId = getReceiverId(chat, authentication);

    messageRepository.setMessagesToSeen(chat, MessageState.READ);

    //to do: notifications
  }


  private String getReceiverId(Chat chat, Authentication authentication){
    if(chat.getSender().getId().equals(authentication.getName())){
     return chat.getReceiver().getId();
    }
    return chat.getSender().getId();
  }

  private String getSenderId(Chat chat, Authentication authentication){
    if(chat.getSender().getId().equals(authentication.getName())){
      return chat.getSender().getId();
    }
    return chat.getReceiver().getId();
  }


  public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication){
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException(("Chat not found")));

    final String senderId = getSenderId(chat, authentication);
    final String receiverId = getReceiverId(chat, authentication);
    final String filePath = fileService.saveFile(file, senderId);

    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

    User receiver = userRepository.findById(receiverId)
      .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

    Message message = Message.builder()
      .sender(sender)
      .receiver(receiver)
      .state(MessageState.SENT)
      .type(MessageType.IMAGE)
      .mediaFilePath(filePath)
      .chat(chat)
      .build();
    messageRepository.save(message);

  }

}
