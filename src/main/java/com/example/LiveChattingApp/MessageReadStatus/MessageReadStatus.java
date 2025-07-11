package com.example.LiveChattingApp.MessageReadStatus;

import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageState;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_read_status")
public class MessageReadStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  private Message message;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Enumerated(EnumType.STRING)
  private MessageState state; // SENT, DELIVERED, READ

  @Column(name = "is_read")
  private boolean isRead;


}
