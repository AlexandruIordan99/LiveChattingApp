package com.example.LiveChattingApp.friendship.DTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDTO {

  private Integer id;
  private Integer userId;
  private String username;
  private String email;
  private String status;
  private String createdAt;
  private boolean isRequester;

}
