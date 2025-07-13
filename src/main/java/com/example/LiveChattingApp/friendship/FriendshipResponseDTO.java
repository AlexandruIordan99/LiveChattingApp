package com.example.LiveChattingApp.friendship;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDTO {

  private String id;
  private String userId;
  private String displayName;
  private String email;
  private String status;
  private String createdAt;
  private boolean isRequester;

}
