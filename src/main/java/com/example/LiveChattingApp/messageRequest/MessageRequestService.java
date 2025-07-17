package com.example.LiveChattingApp.messageRequest;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageRepository;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor

public class MessageRequestService {

  private final MessageRequestRepository messageRequestRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;

  public MessageRequest getOrCreateMessageRequest(MessageRequest request, String senderId, String receiverId) {
    Optional<MessageRequest> existingRequest =
      messageRequestRepository.findBySenderIdAndReceiverIdAndMessageRequestStatus(
        request.getSenderId(), request.getReceiverId(), MessageRequestStatus.PENDING);

    if (existingRequest.isPresent()) {
      MessageRequest existing = existingRequest.get();
      existing.getFirstMessages().addAll(request.getFirstMessages());
      return messageRequestRepository.save(existing);
    }

    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .receiverId(receiverId)
      .status(MessageRequestStatus.PENDING)
      .firstMessages(request.getFirstMessages())
      .type(request.getType())
      .build();

    return messageRequestRepository.save(newRequest);
  }

  public void extractMessageRequestContent(MessageRequest request, Long chatId) {
    if(request.getStatus() != MessageRequestStatus.ACCEPTED){
      throw new IllegalArgumentException("Can only process accepted requests.");
    }

    User sender = userRepository.findById(request.getSenderId())
      .orElseThrow(() -> new EntityNotFoundException("Sender not found."));
    Chat chat =  chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found."));

    for(Message m : request.getFirstMessages()) {
      Message message = Message.builder()
        .sender(sender)
        .chat(chat)
        .content(request.getFirstMessages().toString())
        .build();

      messageRepository.save(message);

    }
    messageRequestRepository.delete(request);
  }

  public void acceptMessageRequest(MessageRequest request, Long chatId){
    request.setStatus(MessageRequestStatus.ACCEPTED);
    messageRequestRepository.save(request);
  }

  public void declineMessageRequest(MessageRequest request, Long chatId){
    request.setStatus(MessageRequestStatus.DECLINED);
    messageRequestRepository.delete(request);
  }

}


