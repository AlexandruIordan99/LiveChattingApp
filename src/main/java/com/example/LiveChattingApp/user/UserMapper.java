package com.example.LiveChattingApp.user;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserMapper {

  public User fromTokenAttributes(Map<String, Object> attributes) {
    User user = new User();

    if (attributes.containsKey("given_name")) {
      user.setFirstname(attributes.get("given_name").toString());
    } else if (attributes.containsKey("nickname")) {
      user.setDisplayName(attributes.get("nickname").toString());
    }

    if (attributes.containsKey("family_name")){
      user.setLastname(attributes.get("family_name").toString());
    }

    if (attributes.containsKey("email")){
      user.setEmail(attributes.get("email").toString());
    }

    user.setLastSeenOnline(LocalDateTime.now());

    return user;
  }

  public UserResponse toUserResponse(User user){
    return UserResponse.builder()
      .id(user.getId())
      .firstName(user.getFirstname())
      .lastName(user.getLastname())
      .displayName(user.getDisplayName())
      .email(user.getEmail())
      .lastSeen(user.getLastSeenOnline())
      .isOnline(user.isUserOnline())
      .build();

  }
}
