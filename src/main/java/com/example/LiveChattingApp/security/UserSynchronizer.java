package com.example.LiveChattingApp.security;

import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserMapper;
import com.example.LiveChattingApp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserSynchronizer {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public void synchronizeWithIdp(Jwt token) {
    try {
      log.info("Synchronizing user with identity provider.");
      getUserEmail(token).ifPresent(userEmail -> {
        log.info("Synchronizing user with email {}", userEmail);
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        if (optionalUser.isPresent()) {
          User existingUser = optionalUser.get();
          if (shouldSyncUser(existingUser)) {
            updateUserFromToken(existingUser, token.getClaims());
            userRepository.save(existingUser);
            log.info("Updated existing user: {}", userEmail);
          }
        } else {
          User newUser = userMapper.fromTokenAttributes(token.getClaims());
          userRepository.save(newUser);
          log.info("Created new user: {}", userEmail);
        }
      });
    } catch (Exception e) {
      log.error("Error synchronizing user with IDP", e);
    }
  }

  private boolean shouldSyncUser(User user) {
    return user.getLastSyncTime() == null ||
      user.getLastSyncTime().isBefore(LocalDateTime.now().minusHours(1));
  }

  private Optional<String> getUserEmail(Jwt token){
    Map<String, Object> attributes = token.getClaims();

    if(attributes.containsKey("email")){
      return Optional.of(attributes.get("email").toString());
    }
    return Optional.empty();

  }

  private void updateUserFromToken(User user, Map<String, Object> claims) {
    if (claims.containsKey("given_name")) {
      user.setFirstname(claims.get("given_name").toString());
    }
    if (claims.containsKey("family_name")) {
      user.setLastname(claims.get("family_name").toString());
    }
    if (claims.containsKey("nickname")) {
      user.setDisplayName(claims.get("nickname").toString());
    }
    user.setLastSeenOnline(LocalDateTime.now());
    user.setLastSyncTime(LocalDateTime.now()); // Add this field to User entity
  }

}
