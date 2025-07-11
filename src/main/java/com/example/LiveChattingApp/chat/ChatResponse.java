package com.example.LiveChattingApp.chat;


import com.example.LiveChattingApp.ChatParticipant.ChatParticipantResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {

  private String id;
  private String name;
  private long unreadCount;
  private String lastMessage;
  private LocalDateTime lastMessageTime;
  private boolean isRecipientOnline;
  private String senderId;
  private String receiverId;
  private List<ChatParticipantResponse> participants;

}
