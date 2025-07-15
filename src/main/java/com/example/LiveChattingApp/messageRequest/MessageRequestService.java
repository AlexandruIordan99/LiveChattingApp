package com.example.LiveChattingApp.messageRequest;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor

public class MessageRequestService {

  private final MessageRequestRepository messageRequestRepository;

  public MessageRequest createMessageRequest(MessageRequest request) {
    Optional<MessageRequest> existingRequest =
      messageRequestRepository.findBySenderIdAndReceiverIdAndMessageRequestStatus(
        request.getSenderId(), request.getReceiverId(), MessageRequestStatus.PENDING);

    if (existingRequest.isPresent()) {
      existingRequest.get().setFirstMessages(request.getFirstMessages());
      return messageRequestRepository.save(existingRequest.get());
    }

    request.setStatus(MessageRequestStatus.PENDING);
    return messageRequestRepository.save(request);
  }

  }



