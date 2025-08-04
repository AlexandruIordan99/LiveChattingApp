package com.example.LiveChattingApp.friendship;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDTO {

  private Long id;
  private Long userId;
  private String displayName;
  private String email;
  private String status;
  private String createdAt;
  private boolean isRequester;

}
