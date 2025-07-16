package com.example.LiveChattingApp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public List<UserResponse> getAllUsersExceptSelf(Authentication connectedUser){
    return userRepository.findAllUsersExceptSelf(connectedUser.getName())
      .stream()
      .map(userMapper::toUserResponse)
      .toList();

  }

  public Optional<User> findById(String userId){
    return userRepository.findById(userId);
  }

  public Optional<User> findByDisplayName(String displayName){
    return  userRepository.findByDisplayName(displayName);
  }

}
