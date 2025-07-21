package com.example.LiveChattingApp.user;


import lombok.*;

@Getter
@Setter
@Builder
public class UserDTO {

  private String id;
  private String email;
  private String displayName;

}
