package com.example.LiveChattingApp.messageRequest;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor

public class MessageRequestService {

  private final MessageRequestRepository messageRequestRepository;

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

  }



