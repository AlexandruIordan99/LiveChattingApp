package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.user.User;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ChatMessage {

  private User sender;
  private String content;
  private MessageType type;

}
