package com.example.LiveChattingApp.message;

import org.springframework.stereotype.Service;

@Service
public class MessageMapper {

  public MessageResponse toMessageResponse(Message message){
    return MessageResponse.builder()
      .id(message.getId())
      .content(message.getContent())
      .senderId(message.getSender().getId())
      .receiverId(message.getReceiver().getId())
      .type(message.getType())
      .state(message.getState())
      .createdAt(message.getCreatedDate())
      .build();
  }

}
