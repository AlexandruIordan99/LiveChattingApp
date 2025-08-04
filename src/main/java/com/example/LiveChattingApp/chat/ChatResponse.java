package com.example.LiveChattingApp.chat;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {

  private Long id;
  private String name;
  private long unreadCount;
  private String lastMessage;
  private LocalDateTime lastMessageTime;
  private boolean isRecipientOnline;
  private Long  senderId;
  private Long  receiverId;

}
