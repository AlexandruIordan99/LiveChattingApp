package com.example.LiveChattingApp.ChatParticipant;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="chat_participant")

public class ChatParticipant extends BaseAuditingEntity {


  @Id
  private String id;
  @ManyToOne
  @JoinColumn(name = "chat_id")
  private Chat chat;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  private ParticipantRole role;

  private LocalDateTime joinedAt;
  private LocalDateTime lastReadAt;

  @ManyToOne
  @JoinColumn(name = "added_by_id")
  private User addedBy;

  private boolean isActive;
}

