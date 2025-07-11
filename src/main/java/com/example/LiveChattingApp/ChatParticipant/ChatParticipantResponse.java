package com.example.LiveChattingApp.ChatParticipant;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatParticipantResponse {

  private String id;
  private String userId;
  private String userName;
  private String displayName;
  private ParticipantRole role;
  private LocalDateTime joinedAt;
  private boolean isOnline;
  private String addedByName;
}
