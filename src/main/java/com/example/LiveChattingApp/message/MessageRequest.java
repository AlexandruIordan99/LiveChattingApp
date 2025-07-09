package com.example.LiveChattingApp.message;


import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

  private String chatId;
  private String senderId;
  private String receiverId;
  private String content;
  private MessageType type;

}
