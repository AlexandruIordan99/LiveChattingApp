package com.example.LiveChattingApp.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

  private String id;
  private String content;
  private MessageType type;
  private String senderId;
  private String receiverId;
  private LocalDateTime createdAt;
  private byte[] media;


}
