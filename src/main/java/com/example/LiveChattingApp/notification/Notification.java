package com.example.LiveChattingApp.notification;

import com.example.LiveChattingApp.message.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Notification {

  private Long chatId;
  private String content;
  private String senderId;
  private String receiverId;
  private String chatName;
  private MessageType messageType;
  private NotificationType type;
  private byte[] media;

}
