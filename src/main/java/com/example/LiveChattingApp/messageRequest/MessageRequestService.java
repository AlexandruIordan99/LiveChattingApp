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

import java.util.ArrayList;
import java.util.Optional;


@Service
@RequiredArgsConstructor

public class MessageRequestService {

  private final MessageRequestRepository messageRequestRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;

  public MessageRequest createMessageRequest(Long senderId, Long chatId) {

    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found."));

    MessageRequest newRequest = MessageRequest.builder()
      .senderId(senderId)
      .chat(chat)
      .status(MessageRequestStatus.PENDING)
      .build();

    return messageRequestRepository.save(newRequest);
  }

  public void addToFirstMessages(Long requestId, Message message){

    MessageRequest request = messageRequestRepository.findById(requestId)
      .orElseThrow(()-> new EntityNotFoundException("Message request not found."));

    ArrayList<Message> firstMessages = request.getFirstMessages();

    if (firstMessages.size() >= 15){
      throw new IllegalStateException("You've reached the maximum number " +
        "of messages before the user accepts your request.");
    }

    firstMessages.add(message);
    messageRequestRepository.save(request);
  }

  public Optional<MessageRequest> findExistingRequest(Long senderId, Long receiverId) {
    return messageRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
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
        .content(m.getContent())
        .type(m.getType())
        .state(m.getState())
        .chat(chat)
        .content(request.getFirstMessages().toString())
        .build();

      messageRepository.save(message);

    }
    messageRequestRepository.delete(request);
  }

  public void acceptMessageRequest(MessageRequest request){
    if(request.getStatus() == MessageRequestStatus.ACCEPTED){
      return;
    }
    request.setStatus(MessageRequestStatus.ACCEPTED);
    messageRequestRepository.save(request);
  }

  public void declineMessageRequest(MessageRequest request){
    if(request.getStatus() == MessageRequestStatus.DECLINED){
      return;
    }
    request.setStatus(MessageRequestStatus.DECLINED);
    messageRequestRepository.delete(request);
  }

}


