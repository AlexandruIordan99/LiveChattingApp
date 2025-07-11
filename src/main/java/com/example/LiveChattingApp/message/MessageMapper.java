package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatus;
import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatusResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class MessageMapper {

  public MessageResponse toMessageResponse(Message message, String currentUserId){
    MessageResponse response = MessageResponse.builder()
      .id(message.getId())
      .content(message.getContent())
      .senderId(message.getSender().getId())
      .type(message.getType())
      .state(message.getState())
      .createdAt(message.getCreatedDate())
      .mediaFilePath(message.getMediaFilePath())
      .build();


    if (message.getReadStatuses() != null) {
      List<MessageReadStatusResponse> readStatusResponses = message.getReadStatuses()
        .stream()
        .map(this::toMessageReadStatusResponse)
        .collect(Collectors.toList());
      response.setReadStatuses(readStatusResponses);
    }

    if (message.getReplyTo() != null) {
      response.setReplyTo(toMessageResponse(message.getReplyTo(), currentUserId));
    }

    return response;

  }

  private MessageReadStatusResponse toMessageReadStatusResponse(MessageReadStatus readStatus) {
    MessageReadStatusResponse response = new MessageReadStatusResponse();
    response.setUserId(readStatus.getUser().getId());
    response.setUserName(readStatus.getUser().getDisplayName());
    response.setState(readStatus.getState());
    response.setReadAt(readStatus.getReadAt());
    return response;
  }
}
