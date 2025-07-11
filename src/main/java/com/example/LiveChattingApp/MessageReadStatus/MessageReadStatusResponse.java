package com.example.LiveChattingApp.MessageReadStatus;

import com.example.LiveChattingApp.message.MessageState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageReadStatusResponse {

  private String userId;
  private String userName;
  private MessageState state;
  private LocalDateTime readAt;

}
