package com.example.LiveChattingApp.message;


import org.springframework.stereotype.Service;



@Service
public class MessageMapper {

  public MessageResponse toMessageResponse(Message message, String currentUserId) {
    MessageResponse response = MessageResponse.builder()
      .id(message.getId())
      .senderId(currentUserId)
      .content(message.getContent())
      .senderId(message.getSender().getId())
      .type(message.getType())
      .state(message.getState())
      .createdAt(message.getCreatedDate())
      .mediaFilePath(message.getMediaFilePath())
      .build();

    return response;

  }

}
