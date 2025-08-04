package com.example.LiveChattingApp.message;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageResponse {

  private Long id;
  private String content;
  private MessageType type;
  private Long senderId;
  private Long receiverId;
  private MessageState state;
  private LocalDateTime createdAt;
  private String mediaFilePath;
  private byte[] media;
  private MessageResponse replyTo;
}
