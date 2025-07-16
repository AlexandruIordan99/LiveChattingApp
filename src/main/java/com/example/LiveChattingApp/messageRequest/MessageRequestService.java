package com.example.LiveChattingApp.messageRequest;


import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageRepository;
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

  public void processMessageRequest(MessageRequest request, String chatId) {
    if (request.getStatus() == MessageRequestStatus.ACCEPTED) {
      Message message = new Message().builder()
        .sender(userRepository.findById(request.getSenderId())
          .orElseThrow(() -> new EntityNotFoundException("Sender not found.")))
        .chat(chatRepository.findById(chatId)
          .orElseThrow(() -> new EntityNotFoundException("Chat not found.")))
        .content(request.getFirstMessages().toString())
        .build();

      messageRepository.save(message);
      messageRequestRepository.delete(request);
    } else if (request.getStatus() == MessageRequestStatus.DECLINED) {
      messageRequestRepository.delete(request);
    }

  }
}


