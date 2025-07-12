package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatusResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageResponse {

  private String id;
  private String content;
  private MessageType type;
  private String senderId;
  private String receiverId;
  private MessageState state;
  private LocalDateTime createdAt;
  private String mediaFilePath;
  private byte[] media;
  private MessageResponse replyTo;
}
