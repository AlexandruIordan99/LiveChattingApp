package com.example.LiveChattingApp.friendship.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
