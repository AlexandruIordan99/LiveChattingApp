package com.example.LiveChattingApp.messageRequest;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_request")
public class MessageRequest {

  @Id
  @SequenceGenerator(name = "msg_seq", sequenceName = "msg_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_seq")
  private Long id;

  @ManyToOne
  @JoinColumn(name="chat_id")
  private Chat chat;

  private String senderId;
  private String receiverId;
  private MessageType type;

  @Enumerated(EnumType.STRING)
  private MessageRequestStatus status;
  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "message_request_id")
  private List<Message> firstMessages;




}
