package com.example.LiveChattingApp.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

  private Long id;
  private String firstName;
  private String lastName;
  private String displayName;
  private String email;
  private LocalDateTime lastSeen;
  private boolean isOnline;

}
