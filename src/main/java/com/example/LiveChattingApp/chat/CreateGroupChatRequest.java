package com.example.LiveChattingApp.chat;


import lombok.Data;

import java.util.Set;

@Data
public class CreateGroupChatRequest {

  private String name;
  private String description;
  private Set<String> participantIds;

}
